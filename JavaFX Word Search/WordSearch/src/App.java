// App.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * The App class is the entry point for the Word Search application. It extends the JavaFX Application class 
 *  to set up the primary stage and display the game board.
 * 
 */
public class App extends Application {
public App() {
    super();
}
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GameBoard gameBoard = new GameBoard();

        primaryStage.setTitle("Word Search");
        primaryStage.setScene(new Scene(gameBoard.createVBox(), 500, 380));
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(380);
        primaryStage.show();
    }
}