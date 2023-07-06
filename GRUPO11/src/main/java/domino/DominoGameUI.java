package domino;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class DominoGameUI extends Application {
    private static final int TILE_SIZE = 60;
    private static final int NUM_PLAYERS = 2;
    private static final int NUM_TILES_PER_PLAYER = 7;

    private BorderPane root;
    private VBox player1TilesBox;
    private VBox player2TilesBox;
    private VBox boneyardBox;
    private Text turnText;
    private Text scoreText;
    private Button drawButton;

    private Random random;
    private int currentPlayer;
    private int player1Score;
    private int player2Score;
    private List<Pane> gameAreaTiles;

    private int boneyardSize;
    private int roundScore;
    private Button startButton;
    Button skipTurnPlayer1;
    Button skipTurnPlayer2;
    private boolean isGameLocked;
    private boolean isRoundOver;
    private int currentRound;
    Text roundText;


    private static final String DB_URL = "jdbc:mysql://192.168.1.8:3306/game";
    private static final String DB_USERNAME = "newuser";
    private static final String DB_PASSWORD = "12345";

    // Prepared statements for database operations
    private PreparedStatement insertPlayer1TileStmt;
    private PreparedStatement insertPlayer2TileStmt;
    private PreparedStatement insertGameAreaTileStmt;
    private PreparedStatement updatePlayerScoresStmt;
    PreparedStatement deletestmt;
    PreparedStatement getPlayer1TileStmt;
    PreparedStatement getPlayer2TileStmt;
    PreparedStatement getGameAreaTileStmt;
    ResultSet rs;
    ResultSet rs1;
    ResultSet rs2;
    ResultSet rs3;
    ResultSet rs4;
    PreparedStatement delPlayer1tile;
    PreparedStatement delPlayer2tile;
    PreparedStatement delGameAreaTile;
    String player;
    PreparedStatement updatestatus;


    PreparedStatement getcurr;
    PreparedStatement updatecurr;
    private VBox gameAreaBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException {
        primaryStage.setTitle("Domino Game");


    }

    private Pane createRandomTile(int player) {
        int value1 = random.nextInt(7);
        int value2 = random.nextInt(7);
        Pane tilePane = createTile(value1, value2, player);
        tilePane.setOnMouseClicked(e -> handleTileClick(tilePane));
        return tilePane;
    }


    // this is ok
    private Pane createTile(int value1, int value2, int player) {
        Pane tilePane = new Pane();
        tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);

        Rectangle tileShape = new Rectangle(TILE_SIZE, TILE_SIZE);
        tileShape.setFill(Color.WHITE);
        tileShape.setStroke(Color.BLACK);

        Text text1 = new Text(Integer.toString(value1));
        text1.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        text1.setFill(player == 1 ? Color.RED : Color.BLUE);
        text1.setX(TILE_SIZE * 0.15);
        text1.setY(TILE_SIZE * 0.3);

        Text text2 = new Text(Integer.toString(value2));
        text2.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        text2.setFill(player == 1 ? Color.RED : Color.BLUE);
        text2.setX(TILE_SIZE * 0.15);
        text2.setY(TILE_SIZE * 0.7);

        tilePane.getChildren().addAll(tileShape, text1, text2);

        return tilePane;
    }


    private void adjustTileOrientation(Pane tilePane) {
        if (gameAreaTiles.isEmpty()) {
            return; // No need to adjust orientation for the first tile
        }

        Pane lastTilePane = gameAreaTiles.get(gameAreaTiles.size() - 1);
        int lastValue1 = Integer.parseInt(((Text) lastTilePane.getChildren().get(1)).getText());
        int lastValue2 = Integer.parseInt(((Text) lastTilePane.getChildren().get(2)).getText());
        int currentValue1 = Integer.parseInt(((Text) tilePane.getChildren().get(1)).getText());
        int currentValue2 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());

        if (currentValue1 == lastValue2 || currentValue2 == lastValue2) {
            // Swap values if the second value matches the last value
            swapTileValues(tilePane);
        } else if (currentValue1 == lastValue1 || currentValue2 == lastValue1) {
            // Reverse values if the first value matches the last value
            reverseTileValues(tilePane);
        }
    }

    private void swapTileValues(Pane tilePane) {
        Text value1Text = (Text) tilePane.getChildren().get(1);
        Text value2Text = (Text) tilePane.getChildren().get(2);

        String tempValue = value1Text.getText();
        value1Text.setText(value2Text.getText());
        value2Text.setText(tempValue);
    }

    private void reverseTileValues(Pane tilePane) {
        Text value1Text = (Text) tilePane.getChildren().get(1);
        Text value2Text = (Text) tilePane.getChildren().get(2);

        String tempValue = value1Text.getText();
        value1Text.setText(value2Text.getText());
        value2Text.setText(tempValue);
    }
    

}