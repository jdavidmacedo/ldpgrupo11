package domino;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
    public void start(Stage stage) throws Exception {


    }
}