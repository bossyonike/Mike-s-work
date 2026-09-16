// GameBoard.java
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

/**
 * Represents the game board for a word search game.
 *
 * <p>The GameBoard class creates a 9x9 grid of randomly generated
 * letters. It allows the user to enter a word, checks the word using
 * an online dictionary API, and searches the board in all eight
 * possible directions for the word.</p>
 * When a valid word is found on the board, the letters that make
 * up the word are highlighted.
 *
 * @author Jordon Youngblood, Michael Lawrence, Zoey Clemons, Nick Kyambadde
 */
public class GameBoard {

    /**
     * The number of rows and columns in the word search board.
     */
    private static final int BOARD_SIZE = 9;

    /**
     * The URI used to check whether a word is valid.
     */
    private static final URI DICTIONARY_URI =
            URI.create("https://wordotron.com/api/v1/check-word");

    /**
     * A 2D array containing the buttons that make up
     * the word search board.
     */
    private final Button[][] boardButtons =
            new Button[BOARD_SIZE][BOARD_SIZE];

    /**
     * Text field where the user enters a word to search for.
     */
    private TextField searchTextField = new TextField();

    /**
     * Label that displays the words the user has successfully found.
     */
    private final Label foundWordsLabel =
            new Label("Found Words:\n");

    /**
     * Creates a 9x9 grid containing randomly generated letter buttons.
     *
     * <p>Each button represents one position on the word search board.
     * The buttons are stored in the boardButtons two-dimensional array.
     *
     * returns a GridPane containing the word search buttons
     */
    public GridPane createWordSearchGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Button letterButton = createLetterButton();
                boardButtons[row][col] = letterButton;
                grid.add(letterButton, col, row);
            }
        }

        return grid;
    }

    /**
     * Creates a button containing a randomly selected letter
     * from A through Z.
     *
     * returns a Button containing a randomly generated letter
     */
    private Button createLetterButton() {
        int max = 26;
        int letterNumber = (int) (Math.random() * max);

        Button letterButton =
                new Button("" + (char) ('A' + letterNumber));

        letterButton.setPrefSize(35, 35);

        return letterButton;
    }

    /**
     * Creates the text field used for entering a word.
     *
     * <p>The text field displays a prompt asking the user to enter
     * a word and connects an ActionHandler to process the user's
     * search.
     *
     * returns a configured TextField for word searches
     */
    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setPromptText("Enter a word");
        textField.setPrefWidth(180);

        searchTextField = textField;
        textField.setOnAction(new ActionHandler(this));

        return textField;
    }

    /**
     * Creates the main layout for the word search game.
     *
     * <p>The layout contains the word search grid in the center,
     * the word input field at the bottom, and the found-words
     * sidebar on the right.</p>
     *
     * returns a BorderPane containing the main game layout
     */
    public BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        mainLayout.setCenter(createWordSearchGrid());
        mainLayout.setBottom(createTextField());
        mainLayout.setRight(createSidebar());

        return mainLayout;
    }

    /**
     * Creates the sidebar that displays words found by the user.
     *
     * returns a VBox containing the found-words label
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(150);

        sidebar.setStyle(
                "-fx-background-color: #f4f4f4; "
                + "-fx-border-color: #dcdcdc;"
        );

        foundWordsLabel.setStyle(
                "-fx-font-size: 14px; "
                + "-fx-font-weight: bold;"
        );

        sidebar.getChildren().add(foundWordsLabel);

        return sidebar;
    }

    /**
     * Creates a VBox containing the main game layout.
     *
     * returns a VBox containing the main game interface
     */
    public VBox createVBox() {
        VBox vBox = new VBox();
        vBox.getChildren().add(createMainLayout());

        return vBox;
    }

    /**
     * Searches for a word starting at a specific position and
     * continuing in a specified direction.
     *
     * <p>The rowDelta and colDelta values determine the direction
     * of the search. For example, a rowDelta of 0 and a colDelta
     * of 1 searches horizontally from left to right.
     *
     * @param rowDelta the change in row position for each letter
     * @param colDelta the change in column position for each letter
     * @param word the word being searched for
     * @param startCol the starting column
     * @param startRow the starting row
     * @return true if the entire word is found in the specified
     *         direction; false otherwise
     */
    public boolean searchInDirection(
            int rowDelta,
            int colDelta,
            String word,
            int startCol,
            int startRow) {

        if (word == null || word.isEmpty()) {
            return false;
        }

        Button[] matchedButtons = new Button[word.length()];

        for (int stepIndex = 0; stepIndex < word.length(); stepIndex++) {
            int currentRow =
                    startRow + (stepIndex * rowDelta);
            int currentCol =
                    startCol + (stepIndex * colDelta);

            if (currentRow < 0 || currentRow >= BOARD_SIZE
                    || currentCol < 0
                    || currentCol >= BOARD_SIZE) {
                return false;
            }

            char targetLetter = word.charAt(stepIndex);
            char gridLetter =
                    boardButtons[currentRow][currentCol]
                            .getText().charAt(0);

            if (gridLetter != targetLetter) {
                return false;
            }

            matchedButtons[stepIndex] =
                    boardButtons[currentRow][currentCol];
        }

        for (Button button : matchedButtons) {
            button.setStyle("-fx-background-color: lightgreen;");
        }

        return true;
    }

    /**
     * Returns the URI used to check words against the dictionary API.
     *
     * returns the dictionary API URI
     */
    public URI dictionaryUri() {
        return DICTIONARY_URI;
    }

    /**
     * Handles a word search entered by the user.
     *
     * <p>The method retrieves the word from the text field, sends
     * the word to the dictionary API, and determines whether the
     * word is valid. The search field is temporarily disabled while
     * the API request is being processed.
     */
    public void handleUserSearch() {
        String typedWord =
                searchTextField.getText().trim().toUpperCase();

        if (typedWord.isEmpty()) {
            return;
        }

        searchTextField.setDisable(true);

        String jsonPayload =
                "{\"word\":\"" + escapeJson(typedWord) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(dictionaryUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient.newHttpClient()
                .sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    boolean isValid =
                            response.statusCode() == 200
                            && response.body() != null
                            && response.body()
                                    .replaceAll("\\s", "")
                                    .contains("\"valid\":true");

                    Platform.runLater(
                            () -> finishSearch(typedWord, isValid));
                })
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        searchTextField.setDisable(false);
                        searchTextField.setStyle(
                                "-fx-border-color: red; "
                                + "-fx-background-color: #ffe6e6;"
                        );
                    });

                    return null;
                });
    }

    /**
     * Finishes processing a word search after the dictionary API
     * has responded.
     *
     * <p>If the word is valid, this method searches the board and
     * highlights the word if it is found. The text field background
     * color indicates whether the word was invalid, found, or valid
     * but not present on the board.
     *
     * @param word the word entered by the user
     * @param isValid true if the dictionary API considers the word valid
     */
    private void finishSearch(String word, boolean isValid) {
        searchTextField.setDisable(false);

        if (!isValid) {
            searchTextField.setStyle(
                    "-fx-border-color: red; "
                    + "-fx-background-color: #ffe6e6;"
            );
            return;
        }

        boolean foundOnBoard =
                findAndHighlightWordOnBoard(word);

        if (foundOnBoard) {
            searchTextField.setStyle(
                    "-fx-border-color: green; "
                    + "-fx-background-color: #e6ffed;"
            );

            foundWordsLabel.setText(
                    foundWordsLabel.getText() + word + "\n"
            );
        } else {
            searchTextField.setStyle(
                    "-fx-border-color: orange; "
                    + "-fx-background-color: #fff5e6;"
            );
        }
    }

    /**
     * Escapes special characters in a string so it can safely
     * be placed inside a JSON string.
     *
     * @param value the string to escape
     * returns the escaped JSON-compatible string
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Searches the entire board for a word in all eight possible
     * directions.
     *
     * <p>Before searching, any previous highlights are removed.
     * The method checks horizontal, vertical, and diagonal
     * directions starting from every position on the board.
     *
     * @param word the word to search for
     * returns true if the word is found on the board; false otherwise
     */
    private boolean findAndHighlightWordOnBoard(String word) {

        for (Button[] row : boardButtons) {
            for (Button button : row) {
                button.setStyle("");
            }
        }

        int[] rowDirections =
                {-1, -1, -1, 0, 0, 1, 1, 1};

        int[] colDirections =
                {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int currentRow = 0;
                currentRow < BOARD_SIZE;
                currentRow++) {

            for (int currentCol = 0;
                    currentCol < BOARD_SIZE;
                    currentCol++) {

                for (int directionIndex = 0;
                        directionIndex < rowDirections.length;
                        directionIndex++) {

                    if (searchInDirection(
                            rowDirections[directionIndex],
                            colDirections[directionIndex],
                            word,
                            currentCol,
                            currentRow)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }
}
```


