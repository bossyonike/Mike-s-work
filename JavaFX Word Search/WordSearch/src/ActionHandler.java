import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ActionHandler implements EventHandler<ActionEvent> {
    private final GameBoard gameBoard;

    public ActionHandler(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    @Override
    public void handle(ActionEvent event) {
        gameBoard.handleUserSearch();
    }
}