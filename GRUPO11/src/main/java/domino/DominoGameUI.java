package domino;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private void updateScoreText() {
        scoreText.setText("Player 1: " + player1Score + "   Player 2: " + player2Score);
    }



    private void handleTileClick(Pane tilePane) {
        if (!isRoundOver) {
            boolean isValidMove = false;
            int value1 = Integer.parseInt(((Text) tilePane.getChildren().get(1)).getText());
            int value2 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());

            if (gameAreaTiles.isEmpty()) {
                isValidMove = true; // First tile, no restrictions
            } else {
                Pane lastTilePane = gameAreaTiles.get(gameAreaTiles.size() - 1);
                int lastValue1 = Integer.parseInt(((Text) lastTilePane.getChildren().get(1)).getText());
                int lastValue2 = Integer.parseInt(((Text) lastTilePane.getChildren().get(2)).getText());

                if (value1 == lastValue2 || value2 == lastValue2 ||
                        value1 == lastValue1 || value2 == lastValue1) {
                    if (!isSidePaired(lastTilePane, value1, value2)) {
                        isValidMove = true; // Match found on at least one side and not already paired
                    }
                }
            }

            if (isValidMove) {
                if (currentPlayer == 1 && player1TilesBox.getChildren().contains(tilePane)) {

                    int value11 = Integer.parseInt(((Text) tilePane.getChildren().get(1)).getText());
                    int value22 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());
                    try {
                        insertGameAreaTileStmt.setInt(1, value11);
                        insertGameAreaTileStmt.setInt(2, value22);
                        insertGameAreaTileStmt.executeUpdate();
                        delPlayer1tile.setInt(1, value1);
                        delPlayer1tile.setInt(2, value2);
                        delPlayer1tile.executeUpdate();
                        updatecurr.setInt(1, 2);
                        updatecurr.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // Handle database error
                    }



                    player1TilesBox.getChildren().remove(tilePane);
                    gameAreaTiles.add(tilePane);
                    currentPlayer = 2;
                    turnText.setText("Turn: Player 2");
                    player1TilesBox.setVisible(false);
                    player2TilesBox.setVisible(true);
                    // Pane newPane=createTile()
                    // Adjust the tile orientation based on matching values
                    adjustTileOrientation(tilePane);

                    // Add the tile pane to the game area box and set its position
                    gameAreaBox.getChildren().add(tilePane);
                    tilePane.setLayoutX((gameAreaTiles.size() - 1) % 6 * TILE_SIZE);
                    tilePane.setLayoutY((gameAreaTiles.size() - 1) / 6 * TILE_SIZE);

                    // Enable skip turn button for Player 2
                    skipTurnPlayer2.setDisable(false);
                    boolean flag=false;
                    while (!flag)
                    {
                        try {
                            ResultSet r =getcurr.executeQuery();
                            if(r.next())
                            {
                                if(r.getInt(1)==1)
                                {
                                    flag=true;
                                }
                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (currentPlayer == 2 && player2TilesBox.getChildren().contains(tilePane)) {

                    int value13 = Integer.parseInt(((Text) tilePane.getChildren().get(1)).getText());
                    int value23 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());
                    try {
                        insertGameAreaTileStmt.setInt(1, value1);
                        insertGameAreaTileStmt.setInt(2, value2);
                        insertGameAreaTileStmt.executeUpdate();
                        delPlayer2tile.setInt(1, value1);
                        delPlayer2tile.setInt(2, value2);
                        delPlayer2tile.executeUpdate();
                        updatecurr.setInt(1, 1);
                        updatecurr.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // Handle database error
                    }


                    player2TilesBox.getChildren().remove(tilePane);
                    gameAreaTiles.add(tilePane);
                    currentPlayer = 1;
                    turnText.setText("Turn: Player 1");
                    player2TilesBox.setVisible(false);
                    player1TilesBox.setVisible(true);
                    // Pane newPane=createTile()
                    // Adjust the tile orientation based on matching values
                    adjustTileOrientation(tilePane);

                    // Add the tile pane to the game area box and set its position
                    gameAreaBox.getChildren().add(tilePane);
                    tilePane.setLayoutX((gameAreaTiles.size() - 1) % 6 * TILE_SIZE);
                    tilePane.setLayoutY((gameAreaTiles.size() - 1) / 6 * TILE_SIZE);

                    // Enable skip turn button for Player 1
                    skipTurnPlayer1.setDisable(false);
                    Boolean flag=false;
                    while (!flag)
                    {
                        try {
                            ResultSet r =getcurr.executeQuery();
                            if(r.next())
                            {
                                if(r.getInt(1)==2)
                                {
                                    flag=true;
                                }
                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                calculateRoundScore();
                updateScoreText();
                checkEndOfRound();
                try {
                    updatePlayerScoresStmt.setInt(1, player1Score);
                    updatePlayerScoresStmt.setString(2, "Player 1");
                    updatePlayerScoresStmt.executeUpdate();

                    updatePlayerScoresStmt.setInt(1, player2Score);
                    updatePlayerScoresStmt.setString(2, "Player 2");
                    updatePlayerScoresStmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Handle database error
                }

            }
        }
    }

    private boolean isSidePaired(Pane tilePane, int value1, int value2) {
        int tileValue2 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());

        // Check if the second value of the existing tile can be paired with any value of the current tile
        if (tileValue2 == value1 || tileValue2 == value2) {
            return false;
        }

        return true;
    }
    private void checkEndOfRound() {
        if (player1TilesBox.getChildren().isEmpty() || player2TilesBox.getChildren().isEmpty()) {
            if (!isGameLocked) {
                int roundWinner;
                int roundLoser;
                int roundPoints;

                if (roundScore == 0) {
                    isGameLocked = true;
                    roundWinner = currentPlayer == 1 ? 2 : 1;
                    roundLoser = currentPlayer;
                    roundPoints = calculateHandScore(roundLoser);
                    System.out.println("Game is locked. All points are counted for each player.");
                } else {
                    roundWinner = currentPlayer;
                    roundLoser = currentPlayer == 1 ? 2 : 1;
                    roundPoints = roundScore + calculateHandScore(roundLoser);
                    System.out.println("Player " + roundWinner + " wins the round and gets " + roundPoints + " points.");
                }

                if (roundPoints > 0) {
                    if (roundWinner == 1) {
                        player1Score += roundPoints;
                    } else {
                        player2Score += roundPoints;
                    }
                    updateScoreText();
                }

                if (player1Score >= 50 || player2Score >= 50) {
                    String winner = player1Score >= 50 ? "Player 1" : "Player 2";
                    String message = winner + " wins the game!";
                    showAlert(AlertType.INFORMATION, "Game Over", message);
                    Platform.exit(); // Exit the application after winning the game
                } else {
                    // Display a prompt to start a new round
                    String message = "Round " + currentRound + " is over. Do you want to start a new round?";
                    ButtonType startNewRoundButton = new ButtonType("Start New Round");
                    ButtonType exitButton = new ButtonType("Exit");
                    showAlert(AlertType.CONFIRMATION, "Round Over", message, startNewRoundButton, exitButton);
                }
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void showAlert(AlertType alertType, String title, String message, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(buttonTypes);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypes[0]) {
                resetRound();
            } else {
                Platform.exit(); // Exit the application if "Exit" button is clicked
            }
        });
    }
    private void resetRound() {
        // Reset round-specific variables and UI elements
        roundScore = 0;
        isRoundOver = false;
        gameAreaTiles.clear();
        gameAreaBox.getChildren().clear();
        currentRound++;
        roundText.setText("Round: " + currentRound);
        currentPlayer = 1;
        turnText.setText("Turn: Player " + currentPlayer);
        // Clear player tiles
        player1TilesBox.getChildren().clear();
        player2TilesBox.getChildren().clear();
        // Generate new random tiles for players
        for (int i = 0; i < NUM_TILES_PER_PLAYER; i++) {
            player1TilesBox.getChildren().add(createRandomTile(1));
            player2TilesBox.getChildren().add(createRandomTile(2));
        }
    }

    private int calculateRoundScore() {
        int roundScore = 0;
        for (Pane tilePane : gameAreaTiles) {
            int value1 = Integer.parseInt(((Text) tilePane.getChildren().get(1)).getText());
            int value2 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());
            roundScore += value1 + value2;
        }
        return roundScore;
    }

    private int calculateHandScore(int player) {
        int handScore = 0;
        VBox playerTilesBox = player == 1 ? player1TilesBox : player2TilesBox;
        for (Node tileNode : playerTilesBox.getChildren()) {
            Pane tilePane = (Pane) tileNode;
            int value1 = Integer.parseInt(((Text) tilePane.getChildren().get(1)).getText());
            int value2 = Integer.parseInt(((Text) tilePane.getChildren().get(2)).getText());
            handScore += value1 + value2;
        }
        return handScore;
    }

    private void resetGame() {
        currentPlayer = 1;
        gameAreaTiles.clear();
        player1TilesBox.getChildren().clear();
        player2TilesBox.getChildren().clear();
        scoreText.setText("Player 1: " + player1Score + "   Player 2: " + player2Score);
        turnText.setText("Turn: Player 1");

        for (int i = 0; i < NUM_TILES_PER_PLAYER; i++) {
            player1TilesBox.getChildren().add(createRandomTile(1));
            player2TilesBox.getChildren().add(createRandomTile(2));
        }

        boneyardSize = 28 - (NUM_TILES_PER_PLAYER * NUM_PLAYERS);
        isGameLocked = false;
        isRoundOver = false;
        player1TilesBox.setVisible(true);
        player2TilesBox.setVisible(false);
    }


    private void drawTile() {
        if (!isGameLocked && !isRoundOver) {
            int player = currentPlayer;
            VBox playerTilesBox = player == 1 ? player1TilesBox : player2TilesBox;
            if (playerTilesBox.getChildren().size() < NUM_TILES_PER_PLAYER) {
                if (boneyardSize > 0) {
                    Pane tilePane = createRandomTile(player);
                    playerTilesBox.getChildren().add(tilePane);
                    boneyardSize--;
                    if (boneyardSize == 0) {
                        drawButton.setDisable(true);
                    }
                    checkEndOfRound(); // Check if the round ends after drawing a tile
                } else {
                    System.out.println("Boneyard is empty.");
                }
            }
        }
    }

}