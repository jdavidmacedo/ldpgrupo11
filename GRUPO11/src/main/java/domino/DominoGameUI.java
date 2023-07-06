package domino;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.application.Platform;

import javax.xml.transform.Result;


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

        // Initialize database connection and prepared statements
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            getPlayer1TileStmt= conn.prepareStatement("SELECT * FROM player1_tiles");
            getPlayer2TileStmt= conn.prepareStatement("SELECT * FROM player2_tiles");
            getGameAreaTileStmt= conn.prepareStatement("SELECT * FROM game_tiles");
            rs=getPlayer1TileStmt.executeQuery();
            rs1=getPlayer2TileStmt.executeQuery();
            rs2=getGameAreaTileStmt.executeQuery();
            updatecurr=conn.prepareStatement("UPDATE current SET curr=?");
            PreparedStatement checkplayer1=conn.prepareStatement("SELECT * FROM active where player=1");
            PreparedStatement checkplayer2=conn.prepareStatement("SELECT * FROM active where player=2");
            rs3=checkplayer1.executeQuery();
            rs4=checkplayer2.executeQuery();
            getcurr=conn.prepareStatement("SELECT * FROM current");

            updatestatus=conn.prepareStatement("UPDATE active SET active=? WHERE player=?");
            delPlayer1tile=conn.prepareStatement("DELETE FROM player1_tiles WHERE value1=? AND value2=?");
            delPlayer2tile=conn.prepareStatement("DELETE FROM player2_tiles WHERE value1=? AND value2=?");
            delGameAreaTile=conn.prepareStatement("DELETE FROM game_tiles WHERE value1=? AND value2=?");
            insertPlayer1TileStmt = conn.prepareStatement("INSERT INTO player1_tiles (value1, value2) VALUES (?, ?)");
            insertPlayer2TileStmt = conn.prepareStatement("INSERT INTO player2_tiles (value1, value2) VALUES (?, ?)");
            insertGameAreaTileStmt = conn.prepareStatement("INSERT INTO game_tiles (value1, value2) VALUES (?, ?)");
            updatePlayerScoresStmt = conn.prepareStatement("UPDATE player_scores SET score = ? WHERE player = ?");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database connection error
        }
        if(rs3.next() && rs3.getInt("active")==0 )
        {


                player="player1";
                updatestatus.setInt(1,1);
                updatestatus.setInt(2,1);
                updatestatus.executeUpdate();






        }
        else if(rs4.next() && rs4.getInt("active")==0)
        {

                player="player2";
                updatestatus.setInt(1,1);
                updatestatus.setInt(2,2);
                updatestatus.executeUpdate();



        }
        else
        {

            Alert a=new Alert(AlertType.INFORMATION);
            a.setContentText("All servers are busy");
            a.showAndWait();
            Platform.exit();

        }




        random = new Random();
        currentPlayer = 1;
        player1Score = 0;
        player2Score = 0;
        currentRound = 1;

        root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: lightgray"); // Add background color to the root pane

        // Top part - Score, Turn, and Skip Turn buttons
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER);
        scoreText = new Text("Player 1: " + player1Score + "   Player 2: " + player2Score);
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        turnText = new Text("Turn: Player " + currentPlayer);
        turnText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        roundText = new Text("Round: " + currentRound);
        roundText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        skipTurnPlayer1 = new Button("Skip Turn (Player 1)");
        skipTurnPlayer1.setOnAction(e -> {
            if (currentPlayer == 1) {
                currentPlayer = 2;
                try {
                    updatecurr.setInt(1, 2);
                    updatecurr.executeUpdate();

                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                turnText.setText("Turn: Player " + currentPlayer);
                player1TilesBox.setVisible(false);
                player2TilesBox.setVisible(true);
            }
        });

        skipTurnPlayer2 = new Button("Skip Turn (Player 2)");
        skipTurnPlayer2.setOnAction(e -> {
            if (currentPlayer == 2) {
                currentPlayer = 1;
try {
                    updatecurr.setInt(1, 1);
                    updatecurr.executeUpdate();

                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                turnText.setText("Turn: Player " + currentPlayer);
                player1TilesBox.setVisible(true);
                player2TilesBox.setVisible(false);
            }
        });
        if(player.equals("player1"))
        {
            skipTurnPlayer2.setVisible(false);
        }
        else
        {
          skipTurnPlayer1.setVisible(false);
        }
        topBox.getChildren().addAll(scoreText, turnText, roundText, skipTurnPlayer1, skipTurnPlayer2);
        root.setTop(topBox);

        // Right part - Boneyard
        boneyardBox = new VBox(5);
        boneyardBox.setAlignment(Pos.TOP_RIGHT);
        drawButton = new Button("Draw");
        drawButton.setStyle("-fx-font-size: 16px");
        drawButton.setOnAction(e -> drawTile());
        boneyardBox.getChildren().add(drawButton);
        root.setRight(boneyardBox);

        // Center parts - Player Tiles and Game Area
        StackPane centerPane = new StackPane();
        centerPane.setAlignment(Pos.CENTER);

        HBox centerBox = new HBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerPane.getChildren().add(centerBox);
        root.setCenter(centerPane);

        player1TilesBox = new VBox(10);
        player1TilesBox.setAlignment(Pos.CENTER);
        player1TilesBox.setPrefWidth(TILE_SIZE);
        if(player.equals("player1"))
            centerBox.getChildren().add(player1TilesBox);

        boolean flag=false;
        while(rs.next())
        {
            int value1=rs.getInt("value1");
            int value2=rs.getInt("value2");
            Pane p=createTile(value1, value2, 1);
            p.setOnMouseClicked(e->handleTileClick(p));
            player1TilesBox.getChildren().add(p);
            flag=true;



        }
        if(!flag)
            for (int i = 0; i < NUM_TILES_PER_PLAYER; i++) {
                Pane p= createRandomTile(1);
                p.setOnMouseClicked(e->handleTileClick(p));
                player1TilesBox.getChildren().add(p);
                insertPlayer1TileStmt.setInt(1, Integer.parseInt(((Text) p.getChildren().get(1)).getText()));
                insertPlayer1TileStmt.setInt(2, Integer.parseInt(((Text) p.getChildren().get(2)).getText()));
                insertPlayer1TileStmt.executeUpdate();

            }

        player2TilesBox = new VBox(10);
        player2TilesBox.setAlignment(Pos.CENTER);
        player2TilesBox.setPrefWidth(TILE_SIZE);

        while(rs1.next())
        {
            int value1=rs1.getInt("value1");
            int value2=rs1.getInt("value2");
            Pane p=createTile(value1, value2, 2);
            p.setOnMouseClicked(e->handleTileClick(p));
            player2TilesBox.getChildren().add(p);
            flag=true;
        }
        if(!flag)
            for (int i = 0; i < NUM_TILES_PER_PLAYER; i++) {
                Pane p= createRandomTile(2);
                p.setOnMouseClicked(e->handleTileClick(p));
                player2TilesBox.getChildren().add(p);
                insertPlayer2TileStmt.setInt(1, Integer.parseInt(((Text)p.getChildren().get(1)).getText()));
                insertPlayer2TileStmt.setInt(2, Integer.parseInt(((Text)p.getChildren().get(2)).getText()));
                insertPlayer2TileStmt.executeUpdate();

            }

        gameAreaBox = new VBox(20);
        gameAreaBox.setAlignment(Pos.CENTER);
        gameAreaBox.setPrefWidth(TILE_SIZE * 4);
        gameAreaBox.setPrefHeight(TILE_SIZE * 2);
        gameAreaBox.setStyle("-fx-border-color: black; -fx-border-width: 2px;"); // Add border to the game area
        gameAreaTiles = new ArrayList<>();
        gameAreaBox.getChildren().addAll(gameAreaTiles);
        centerBox.getChildren().add(gameAreaBox);
        if(player.equals("player2"))
            centerBox.getChildren().add(player2TilesBox);
        while(rs2.next())
        {
            int value1=rs2.getInt("value1");
            int value2=rs2.getInt("value2");
            gameAreaBox.getChildren().add(createTile(value1, value2, 0));

        }

        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(e -> {
            try {
                //set status of player 1 to 0
                if(player.equals("player1"))
                {
                    updatestatus.setInt(1, 0 );
                    updatestatus.setInt(2, 1);
                    updatestatus.executeUpdate();


                }
                //set status of player 2 to 0
                else if(player.equals("player2"))
                {
                    updatestatus.setInt(1, 0);
                    updatestatus.setInt(2, 2);
                    updatestatus.executeUpdate();

                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        primaryStage.show();
        boolean flags=false;
        Connection conn=DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

        while(!flags)
        {

            if(player.equals("player1"))
            {
                rs=getcurr.executeQuery();
                while(rs.next())
                {
                    if(rs.getInt("curr")==1)
                    {
                        flags=true;
                        PreparedStatement updategame=conn.prepareStatement("Select * from game_tiles");
                        ResultSet rs3=updategame.executeQuery();
                        gameAreaBox.getChildren().removeAll();
                        while(rs3.next())
                        {
                            int value1=rs3.getInt("value1");
                            int value2=rs3.getInt("value2");
                            gameAreaBox.getChildren().add(createTile(value1, value2, 0));

                        }
                        break;
                    }
                }
            }
            else if(player.equals("player2"))
            {
                rs=getcurr.executeQuery();
                while(rs.next())
                {
                    if(rs.getInt("curr")==2)
                    {
                        flags=true;
                        PreparedStatement updategame=conn.prepareStatement("Select * from game_tiles");
                        ResultSet rs3=updategame.executeQuery();
                        gameAreaBox.getChildren().removeAll();
                        while(rs3.next())
                        {
                            int value1=rs3.getInt("value1");
                            int value2=rs3.getInt("value2");
                            gameAreaBox.getChildren().add(createTile(value1, value2, 0));

                        }
                        break;
                    }
                }
            }

        }


        //resetGame();

        // Start button and draw button event handlers
        startButton = new Button("Start");
        startButton.setStyle("-fx-font-size: 16px");
        startButton.setOnAction(e -> {
            resetGame();
            startButton.setDisable(true);
            drawButton.setDisable(false);
            player1TilesBox.setVisible(true);
            player2TilesBox.setVisible(false);
        });

        drawButton.setOnAction(e -> {
            drawTile();
        });

        // Add the start button to the bottom part of the UI
        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().add(startButton);
        root.setBottom(bottomBox);
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
