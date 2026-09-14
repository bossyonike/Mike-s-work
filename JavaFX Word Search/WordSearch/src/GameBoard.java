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

public class GameBoard {

    private static final int BOARD_SIZE = 9;
    private static final URI DICTIONARY_URI = URI.create("https://wordotron.com/api/v1/check-word");

    private final Button[][] boardButtons = new Button[BOARD_SIZE][BOARD_SIZE];
    private TextField searchTextField = new TextField();
    private final Label foundWordsLabel = new Label("Found Words:\n");

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

    private Button createLetterButton() {
        int max = 26;
        int letterNumber = (int) (Math.random() * max);
        Button letterButton = new Button("" + (char) ('A' + letterNumber));
        letterButton.setPrefSize(35, 35);
        return letterButton;
    }

    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setPromptText("Enter a word");
        textField.setPrefWidth(180);
        searchTextField = textField;
        textField.setOnAction(new ActionHandler(this));
        return textField;
    }

    public BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        mainLayout.setCenter(createWordSearchGrid());
        mainLayout.setBottom(createTextField());
        mainLayout.setRight(createSidebar());
        return mainLayout;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(150);
        sidebar.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dcdcdc;");
        foundWordsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        sidebar.getChildren().add(foundWordsLabel);
        return sidebar;
    }

    public VBox createVBox() {
        VBox vBox = new VBox();
        vBox.getChildren().add(createMainLayout());
        return vBox;
    }

    public boolean searchInDirection(int rowDelta, int colDelta, String word, int startCol, int startRow) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        Button[] matchedButtons = new Button[word.length()];
        for (int stepIndex = 0; stepIndex < word.length(); stepIndex++) {
            int currentRow = startRow + (stepIndex * rowDelta);
            int currentCol = startCol + (stepIndex * colDelta);
            if (currentRow < 0 || currentRow >= BOARD_SIZE
                    || currentCol < 0 || currentCol >= BOARD_SIZE) {
                return false;
            }

            char targetLetter = word.charAt(stepIndex);
            char gridLetter = boardButtons[currentRow][currentCol].getText().charAt(0);
            if (gridLetter != targetLetter) {
                return false;
            }
            matchedButtons[stepIndex] = boardButtons[currentRow][currentCol];
        }

        for (Button button : matchedButtons) {
            button.setStyle("-fx-background-color: lightgreen;");
        }
        return true;
    }

    public URI dictionaryUri() {
        return DICTIONARY_URI;
    }

    public void handleUserSearch() {
        String typedWord = searchTextField.getText().trim().toUpperCase();
        if (typedWord.isEmpty()) {
            return;
        }

        searchTextField.setDisable(true);
        String jsonPayload = "{\"word\":\"" + escapeJson(typedWord) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(dictionaryUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    boolean isValid = response.statusCode() == 200
                            && response.body() != null
                            && response.body().replaceAll("\\s", "").contains("\"valid\":true");
                    Platform.runLater(() -> finishSearch(typedWord, isValid));
                })
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        searchTextField.setDisable(false);
                        searchTextField.setStyle("-fx-border-color: red; -fx-background-color: #ffe6e6;");
                    });
                    return null;
                });
    }

    private void finishSearch(String word, boolean isValid) {
        searchTextField.setDisable(false);
        if (!isValid) {
            searchTextField.setStyle("-fx-border-color: red; -fx-background-color: #ffe6e6;");
            return;
        }

        boolean foundOnBoard = findAndHighlightWordOnBoard(word);
        if (foundOnBoard) {
            searchTextField.setStyle("-fx-border-color: green; -fx-background-color: #e6ffed;");
            foundWordsLabel.setText(foundWordsLabel.getText() + word + "\n");
        } else {
            searchTextField.setStyle("-fx-border-color: orange; -fx-background-color: #fff5e6;");
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean findAndHighlightWordOnBoard(String word) {
        for (Button[] row : boardButtons) {
            for (Button button : row) {
                button.setStyle("");
            }
        }

        int[] rowDirections = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colDirections = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int currentRow = 0; currentRow < BOARD_SIZE; currentRow++) {
            for (int currentCol = 0; currentCol < BOARD_SIZE; currentCol++) {
                for (int directionIndex = 0; directionIndex < rowDirections.length; directionIndex++) {
                    if (searchInDirection(rowDirections[directionIndex], colDirections[directionIndex],
                            word, currentCol, currentRow)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}