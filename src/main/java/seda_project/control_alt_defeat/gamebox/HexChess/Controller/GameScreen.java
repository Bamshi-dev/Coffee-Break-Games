package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurveTo;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.ChessEventListener;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.HexBot;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.HexCell;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.Piece;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceSettings;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceType;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PlayerColor;
import seda_project.control_alt_defeat.gamebox.HexChess.Network.ChessMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

public class GameScreen extends Controller implements Initializable, ChessEventListener {
    PieceSettings settings = PieceSettings.getInstance();
    private GameEngine gameEngine;
    private ImageView endangeredKingView = null;
    private PlayerColor activePlayer;
    private boolean isNetworkPromotion = false;
    private boolean isBotMode = false;
    private PlayerColor botColor = null;
    private boolean disconnected = false;
    private Scene thisScene;
    private List<ImageView>p1Views;
    private List<ImageView>p2Views;
    private boolean lanMode = false;

    @FXML
    private VBox header, p1, p2;

    @FXML
    private HBox p1Buttons, p2Buttons;

    @FXML
    private StackPane stackPane;

    @FXML
    private Pane boardPane;

    @FXML
    private ImageView p1PawnImg, p1RookImg, p1KnightImg, p1BishopImg, p1QueenImg, p1KingImg;
    @FXML
    private ImageView p2PawnImg, p2RookImg, p2KnightImg, p2BishopImg, p2QueenImg, p2KingImg;

    @FXML
    private Label p1Score, p2Score, p1NameLabel, p2NameLabel,activePlayerLabel;

    @FXML
    private Button p1Resign, p1Draw, p2Resign, p2Draw;

    @FXML
    private HBox activePlayerBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        p1Views = Stream.of(p1PawnImg,p1RookImg,p1KnightImg,p1BishopImg,p1QueenImg,p1KingImg).toList();
        p2Views = Stream.of(p2PawnImg,p2RookImg,p2KnightImg,p2BishopImg,p2QueenImg,p2KingImg).toList();
        applyTileColor();
        applyPiecePreviewImages();
    }

    /**
     * Set the names of the players in the according Labels
     * @param p1Name Name of Player 1
     * @param p2Name Name of Player 2
     */
    public void setNames(String p1Name, String p2Name){
        p1NameLabel.setText(p1Name);
        p2NameLabel.setText(p2Name);
        highlightActivePlayer(activePlayer);
    }


    /**
     * Set the points of the players in the according Labels
     * @param p1Points Points of Player 1
     * @param p2Points Points of Player 2
     */
    public void setPoints (double p1Points, double p2Points){
        p1Score.setText(String.valueOf(p1Points));
        p2Score.setText(String.valueOf(p2Points));
    }

    /**
     * Initialize the Game Screen.
     * @param scene the FXML Scene of the GameScreen
     */
    public void init(Scene scene){
        this.thisScene = scene;

        this.gameEngine.addListener(this);
        this.gameEngine.setupInitialState();
        activePlayer = gameEngine.getActivePlayer();

        highlightActivePlayer(activePlayer);

        if (isBotMode) {
            p1.getChildren().remove(p1Buttons);
            p2.getChildren().remove(p2Buttons);
        }
        disableButtons();
    }

    /**
     * initialize the Game Screen with a custom Board State
     * @param boardState The state to start from
     * @param scene the FXML Scene of the GameScreen
     */
    public void init(String boardState, Scene scene) {
        thisScene = scene;
        this.gameEngine.addListener(this);
        this.gameEngine.setupInitalState(boardState);
        activePlayer = gameEngine.getActivePlayer();
        highlightActivePlayer(activePlayer);

        if (isBotMode) {
            p1.getChildren().remove(p1Buttons);
            p2.getChildren().remove(p2Buttons);
        }

        disableButtons();
    }

    /**
     * Assigns the game engine instance that this controller will listen to and delegate moves, promotions, and rule queries to.
     * @param gameEngine the game engine backing this screen
     */
    public void setGameEngine(GameEngine gameEngine){
        this.gameEngine = gameEngine;
    }


    /**
     * Setter for the lan mode
     */
    public void setLanMode(){
        this.lanMode = true;
    }


    /**
     * Disable the Resign and Draw Button for the opponent in the lan mode
     */
    private void disableButtons() {
        if (!lanMode) return;
        Session s = Session.current();
        Button resign = !s.isHost ? p1Resign : p2Resign;
        Button draw = !s.isHost ? p1Draw : p2Draw;

        resign.setDisable(true);
        draw.setDisable(true);
    }

    /**
     * Method from the ChessEventListener that is called whenever a piece is placed at a location during initialization
     * @param id the id of the field the piece is placed on
     * @param piece the piece that is moved
     */
    @Override
    public void onPlaced(String id, Piece piece){
        String pieceId = piecetoString(piece);
        Polygon field = (Polygon) boardPane.lookup("#" + id);

        Image img = getPieceImage(piece);

        ImageView newPiece = new ImageView(img);
        newPiece.setFitWidth(40);
        newPiece.setFitHeight(40);
        newPiece.setPreserveRatio(true);
        newPiece.setUserData(id);
        newPiece.setId(pieceId);

        newPiece.setLayoutX(field.getLayoutX() - 20);
        newPiece.setLayoutY(field.getLayoutY() - 20);
        newPiece.setMouseTransparent(true);
        boardPane.getChildren().add(newPiece);
    }

    /**
     * Apply the TileColor set in the ChessSettings to the actual Board Polygons
     */
    private void applyTileColor() {
        StringBuilder style = new StringBuilder();
        if (settings.getDarkTiles() != null)
            style.append("-dark-poly-color: ").append(toCssColor(settings.getDarkTiles())).append("; ");
        if (settings.getNormalTiles() != null)
            style.append("-normal-poly-color: ").append(toCssColor(settings.getNormalTiles())).append("; ");
        if (settings.getLightTiles() != null)
            style.append("-light-poly-color: ").append(toCssColor(settings.getLightTiles())).append("; ");
        if (!style.isEmpty())
            root.setStyle(style.toString());
    }

    /**
     * Loads the Images of the settings and stores them in a list for an easy access
     */
    private void applyPiecePreviewImages() {
        List<ImageView> p1 = settings.getP1Pieces();
        if (p1 != null && p1.size() >= 6) {
            p1PawnImg.setImage(p1.get(0).getImage());
            p1RookImg.setImage(p1.get(1).getImage());
            p1KnightImg.setImage(p1.get(2).getImage());
            p1BishopImg.setImage(p1.get(3).getImage());
            p1QueenImg.setImage(p1.get(4).getImage());
            p1KingImg.setImage(p1.get(5).getImage());
        }
        List<ImageView> p2 = settings.getP2Pieces();
        if (p2 != null && p2.size() >= 6) {
            p2PawnImg.setImage(p2.get(0).getImage());
            p2RookImg.setImage(p2.get(1).getImage());
            p2KnightImg.setImage(p2.get(2).getImage());
            p2BishopImg.setImage(p2.get(3).getImage());
            p2QueenImg.setImage(p2.get(4).getImage());
            p2KingImg.setImage(p2.get(5).getImage());
        }
    }

    /**
     * Transforms a Color to a hex CSS color string.
     * @param color the color to convert
     * @return the CSS hex color representation
     */
    private static String toCssColor(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
    }

    /**
     * The actual selection and movement method for the game
     * When clicking on a tile with a piece all the possible moves are shown.
     * When clicking on such a tile the move is performed
     * When selecting a different tile the legal moves of this tile are shown
     * @param mouseEvent the mouse click event fired on the clicked board tile (polygon)
     */
    @FXML
    public void handleTileClick(MouseEvent mouseEvent) {
        Session s = Session.current();
        clearHighlights();

        Polygon polygon = (Polygon) mouseEvent.getSource();
        String currentTileId = polygon.getId();

        ImageView pieceOnTile = getPieceAtPolygon(currentTileId);
        if (pieceOnTile != null) {
            PlayerColor playerColor = (pieceOnTile.getId().contains("1")) ? PlayerColor.WHITE : PlayerColor.BLACK;

            if (s.network != null) {
                PlayerColor myColor = s.isHost ? PlayerColor.WHITE : PlayerColor.BLACK;

                if (playerColor != myColor || activePlayer != myColor) {
                    return;
                }
            }

            // enforce turn order
            // Prevent concurrent modifications that would crash the Minimax algorithm (hexbot)
            if (playerColor != activePlayer) {
                return;
            }

            if (isBotMode && playerColor == botColor) {
                return;
            }

            pieceOnTile.getStyleClass().add("selectedTile");
            Piece p = gameEngine.getBoard().getCellById(currentTileId).getPiece();
            List<HexCell> legalMoves = gameEngine.getLegalMoves(p);

            for (HexCell cell :legalMoves ){;
                String targetTileId = cell.getCoords().transformHextoId();

                if(boardPane.lookup("#"+targetTileId)instanceof  Polygon targetPolygon){

                    ImageView pieceToCapture = getPieceAtPolygon(targetTileId);
                    if (pieceToCapture != null) {
                        pieceToCapture.getStyleClass().add("capture");
                    }
                    else {
                        if (gameEngine.isEnpassent() && targetTileId.equals(gameEngine.getEnpassentCoordGhost().transformHextoId())&& p.getType() == PieceType.PAWN){
                            enpassentCoord(targetTileId, gameEngine.getEnpassentMovedTo().transformHextoId());
                        }
                        Circle moveDot = new Circle(10);
                        moveDot.setLayoutX(targetPolygon.getLayoutX());
                        moveDot.setLayoutY(targetPolygon.getLayoutY());
                        if (s.network != null){
                            if (s.isHost){
                                if (playerColor == PlayerColor.WHITE) {
                                    moveDot.getStyleClass().add("selfDot");
                                }
                                else {
                                    moveDot.getStyleClass().add("enemyDot");
                                }
                            }
                            else{
                                if (playerColor == PlayerColor.BLACK){
                                    moveDot.getStyleClass().add("selfDot");
                                }
                                else {
                                    moveDot.getStyleClass().add("enemyDot");
                                }
                            }
                        }
                        else {
                            if (playerColor == activePlayer) {
                                moveDot.getStyleClass().add("selfDot");
                            } else {
                                moveDot.getStyleClass().add("enemyDot");
                            }
                        }
                        moveDot.setMouseTransparent(true);
                        boardPane.getChildren().add(moveDot);
                    }
                    targetPolygon.setOnMouseClicked(event -> {
                        boolean ok = gameEngine.handleMove(currentTileId, targetTileId);
                        clearMove();
                        if (s.network != null) {
                            if (s.isHost) {
                                if (ok) {
                                    s.network.send(new ChessMessage.StateUpdate(currentTileId, targetTileId, null));
                                }
                            } else {
                                s.network.send(new ChessMessage.Input(currentTileId, targetTileId));
                            }
                        }
                        else {
                            if (ok) {
                                playSound();
                            }
                        }
                        clearHighlights();
                        event.consume();
                        for (Node n : boardPane.getChildren()){
                            if (n instanceof Polygon poly){
                                poly.setOnMouseClicked(this::handleTileClick);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Play one of the 5 available sounds if a piece is moved
     */
    private void playSound(){
        Random rand  = new Random();
        int sound = rand.nextInt(1,5);
        String soundName = "chess_move_"+sound;
        sC.play(soundName);
    }

    /**
     * Clear the UI Indicator for the last move
     */
    private void clearMove() {
        for (Node node : boardPane.getChildren()) {
            if (node instanceof Polygon poly) {
                poly.getStyleClass().remove("movedFrom");
                poly.getStyleClass().remove("movedTo");
            }
        }
    }

    /**
     * Clear the UI indicator for the piece highlights and move indicators
     */
    private void clearHighlights() {
        for (Node node : boardPane.getChildren()) {
            if ( node instanceof ImageView iv ){
                iv.getStyleClass().remove("capture");
                iv.getStyleClass().remove("selectedTile");
            }
            if (node instanceof Polygon poly) {
                poly.setOnMouseClicked(this::handleTileClick);
            }
        }

        boardPane.getChildren().removeIf(node -> node instanceof Path || node instanceof Circle);
    }

    /**
     * Setter for the bot mode along with the color the bot plays as.
     * @param isBotMode {@code true} if this game is played against a bot, {@code false} otherwise
     * @param botColor the color controlled by the bot, or {@code null} if not in bot mode
     */
    public void setBotMode(boolean isBotMode, PlayerColor botColor) {
        this.isBotMode = isBotMode;
        this.botColor = botColor;
    }

    /**
     * Method that is called when a piece is moved to a different tile
     * @param fromId id of the tile it has been on
     * @param toId id of the tile it is moved to
     */
    @Override
    public void move(String fromId, String toId) {
        ImageView piece = null;
        Polygon old = null;
        Polygon poly= null;
        for (Node n : boardPane.getChildren()) {
            if (n.getId() != null) {
                if (n.getId().equals(fromId)) {
                    piece = getPieceAtPolygon(fromId);
                    old = (Polygon) n;
                }
                if (n.getId().equals(toId)) {
                    poly = (Polygon) n;
                }
            }
        }
        old.getStyleClass().add("movedFrom");
        poly.getStyleClass().add("movedTo");

        old.setUserData(null);
        piece.setUserData(toId);
        piece.setLayoutX(poly.getLayoutX() - 20);
        piece.setLayoutY(poly.getLayoutY() - 20);
        clearHighlights();
    }


    /**
     * Method that is called whenever a piece is captured
     * @param fromId id of the tile the moving piece is coming from
     * @param toId id of the tile the moving piece is goint to
     * @param capturedPiece the actual piece that is captured
     */
    @Override
    public void capture(String fromId, String toId, Piece capturedPiece) {

        ImageView captured = getPieceAtPolygon(toId);
        boardPane.getChildren().remove(captured);
        move(fromId, toId);
        adaptLabels(capturedPiece);
    }

    /**
     * Changes the Label counter if a piece is captured
     * @param capturedPiece the captured Piece
     */
    private void adaptLabels(Piece capturedPiece) {
        String labelId = piecetoString(capturedPiece).replace("Img", "");
        Label capturedLabel = (Label) thisScene.lookup("#" + labelId);
        if (capturedLabel != null) {
            int current = Integer.parseInt(capturedLabel.getText().replace("x",""));
            String newValue = (labelId.contains("1")) ? "x"+(current + 1):(current + 1)+"x";
            capturedLabel.setText(newValue);
        }
    }

    /**
     * Method that is called if a capture is enpassent
     * @param enpassentId the id of the field the captured pawn is sitting on
     * @param enpassentPiece the enpassent pawn
     */
    @Override
    public void enpassent(String enpassentId, Piece enpassentPiece){
        ImageView img = getPieceAtPolygon(enpassentId);
        boardPane.getChildren().remove(img);
        adaptLabels(enpassentPiece);
    }


    /**
     * Method that is called if the game does end in a win
     * @param player the player that has lost
     */
    @Override
    public void gameEnd(PlayerColor player) {
        int winner = (player == PlayerColor.WHITE) ? 2 : 1;
        handleNetworkGameEnd("WIN", winner);
    }

    /**
     * Method that is called if one player resigns
     * @param playerColor the player that did not resign
     */
    private void resign(PlayerColor playerColor) {
        int winner = (playerColor == PlayerColor.WHITE) ? 1 : 2;
        handleNetworkGameEnd("RESIGN",winner);
    }


    /**
     * Method that is called if the game ends in a remis
     */
    @Override
    public void remis() {
        handleNetworkGameEnd("REMIS", 0);
    }

    /**
     * Method that is called if the game ends by a draw proposal
     */
    public void drawProposal() {
        handleNetworkGameEnd("PROPOSAL", 0);
    }

    /**
     * Method that is called if the game ends in a stalemate
     * @param currentTurn the winner of looser of the stalemate
     */
    @Override
    public void stalemate(PlayerColor currentTurn) {
        int winner = (currentTurn == PlayerColor.WHITE) ? 2 : 1;
        handleNetworkGameEnd("STALEMATE", winner);
    }

    /**
     * Mehtod to handle the game end. Also handles the game End in case of a network game
     * @param reason One of the following: Stalemate, Proposal, Remis, Resign, Win
     * @param winner int of the player that has won
     */
    private void handleNetworkGameEnd(String reason, int winner) {
        Session s = Session.current();

        if (s.network != null) {
            if (s.isHost) {
                s.network.send(new ChessMessage.GameEnded(reason, winner));
                showResultScreen(reason, winner);
            }
            else{
                s.network.send(new ChessMessage.GameEnded(reason, winner));
            }
        } else {
            showResultScreen(reason, winner);
        }
    }

    /**
     * Method to show the result overlay
     * @param reason reason One of the following: Stalemate, Proposal, Remis, Resign, Win
     * @param winner int of the player that has won
     */
    public void showResultScreen(String reason, int winner) {
        p1Draw.setDisable(true);
        p2Draw.setDisable(true);
        p1Resign.setDisable(true);
        p2Resign.setDisable(true);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/HexChess/ResultScreen.fxml"));
            Parent resultOverlay = loader.load();
            ResultScreen controller = loader.getController();
            controller.handData(p1NameLabel.getText(), p2NameLabel.getText(), p1Score.getText(), p2Score.getText());
            controller.setGameScreen(this);
            controller.initNetwork();

            if (flipped)   controller.flip();
            if (rainbowed) controller.rainbow();

            switch (reason) {
                case "WIN" -> controller.winner(winner);
                case "STALEMATE" -> controller.stalemate(winner);
                case "REMIS" -> controller.remis();
                case "PROPOSAL" -> controller.draw();
                case "RESIGN" -> controller.resign(winner);
            }
            stackPane.getChildren().add(resultOverlay);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Result Screen overlay.");
        }
    }

    /**
     * Method that is called whenever a promotion occurs. In case of a network game only the promoting player gets the UI for this
     * @param currentTurn
     */
    @Override
    public void promotion(PlayerColor currentTurn) {
        // Ignore UI. bot always chooses queen when promoted.
        if (isBotMode && currentTurn == botColor) {
            return;
        }

        Session s = Session.current();
        if  (s.network != null) {
            if (s.isHost && currentTurn == PlayerColor.WHITE) {
                prom(currentTurn);
            }
            else if(!s.isHost && currentTurn == PlayerColor.BLACK){
                prom(currentTurn);
            }
        }
        else {
            prom(currentTurn);
        }
    }

    /**
     * the actual promotion window and handling
     * @param currentTurn
     */
    public void prom(PlayerColor currentTurn){
        try {
            Stage promotionStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/HexChess/PromotionStage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) thisScene.getWindow();
            Scene s = new Scene(root);
            s.setFill(Color.TRANSPARENT);
            promotionStage.setScene(s);
            promotionStage.initStyle(StageStyle.TRANSPARENT);
            promotionStage.initModality(Modality.APPLICATION_MODAL);

            promotionStage.setWidth(245);
            promotionStage.setHeight(156);
            promotionStage.setX(stage.getX() + stage.getWidth() / 2 - promotionStage.getWidth() / 2);
            promotionStage.setY(stage.getY() + stage.getHeight() / 2 - promotionStage.getHeight() / 2);
            // If the user closes the window, it will automatically choose queen instead of freezing
            promotionStage.setOnCloseRequest(event -> {
                if (gameEngine.isPromotionPending()) {
                    gameEngine.promote(PieceType.QUEEN);
                }
            });

            promotionStage.show();

            Promotion controller = loader.getController();
            List<ImageView> views = (currentTurn == PlayerColor.WHITE) ? settings.getP1Pieces() : settings.getP2Pieces();
            controller.sendPieces(views, gameEngine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that is called whenever a promotion has been finished
     * @param coordId id of the field where the promotion occured
     * @param piece the new piece
     */
    @Override
    public void onPromoted(String coordId, Piece piece){
        ImageView pawnView = getPieceAtPolygon(coordId);
        if (pawnView == null) return;
        String newPieceId = piecetoString(piece);
        ImageView sourceView = (ImageView) thisScene.lookup("#" + newPieceId);

        if (sourceView != null) {
            pawnView.setImage(sourceView.getImage());
        }

        pawnView.setId(newPieceId);
        Session s = Session.current();
        if (s.network != null && !isNetworkPromotion) {
            s.network.send(new ChessMessage.PromotionChoice(coordId, piece.getType()));
        }
    }

    /**
     * Mehtod that is called if a King is endangered and an action has to be performed to not lose the game
     * @param king
     * @param isEndangered
     */
    @Override
    public void endangered(Piece king, boolean isEndangered) {
        if (endangeredKingView != null) {
            endangeredKingView.getStyleClass().remove("endangered");
            endangeredKingView = null;
        }

        if (!isEndangered) return;

        String kingId = king.getPosition().transformHextoId();
        ImageView kingView = getPieceAtPolygon(kingId);
        if (kingView == null) return;

        kingView.getStyleClass().add("endangered");
        endangeredKingView = kingView;
    }

    /**
     * Method that is called if the player changes. If its the turn of the bot the evaluation of the best move is done and the best move is performed
     * @param currentTurn
     */
    @Override
    public void activePlayer(PlayerColor currentTurn) {
        this.activePlayer = currentTurn;
        highlightActivePlayer(activePlayer);

        if (isBotMode && currentTurn == botColor) {
            String currentFen = gameEngine.getBoard().createNotation(currentTurn);

            new Thread(() -> {
                String[] bestMove = HexBot.getBestMove(currentFen, botColor);

                if (bestMove != null) {
                    javafx.application.Platform.runLater(() -> {
                        // Don't let the bot move if the game was exited
                        if (disconnected) return;
                        gameEngine.handleMove(bestMove[0], bestMove[1]);
                        if (gameEngine.getCurrentTurn() == botColor && !gameEngine.isGameOver()) {

                            // default to Queen if none specified
                            PieceType promoType = PieceType.QUEEN;
                            if (bestMove.length > 2 && !bestMove[2].equals("NONE")) {
                                promoType = PieceType.valueOf(bestMove[2]);
                            }

                            gameEngine.promote(promoType);
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * Indicate the enpassent movement on the UI
     * @param from
     * @param to
     */
    public void enpassentCoord(String from, String to) {
        Polygon fromPoly = (Polygon) boardPane.lookup("#" + from);
        Polygon toPoly = (Polygon) boardPane.lookup("#" + to);

        double fromX = fromPoly.getLayoutX();
        double fromY = fromPoly.getLayoutY();

        double toX = toPoly.getLayoutX();
        double toY = toPoly.getLayoutY();

        double leftX = fromX-10;
        double rightX = fromX+10;

        double radius = 4;


        Path filledArrow = new Path();
        filledArrow.getStyleClass().add("enpassant-arrow");

        double endY = (fromY>toY)? toY+15 : toY-15;
        double controlY = fromY + (toY - fromY) * 0.5;

        filledArrow.getElements().addAll(
                new MoveTo(leftX, fromY),
                new ArcTo(radius, radius, 0, rightX, fromY, false, (fromY > toY)),
                new QuadCurveTo(rightX, controlY, toX, endY),
                new QuadCurveTo(leftX, controlY, leftX, fromY),
                new ClosePath()
        );

        boardPane.getChildren().add(filledArrow);

        ImageView img = getPieceAtPolygon(to);
        img.getStyleClass().add("capture");
    }


    /**
     * Method to exit the game
     */
    @FXML
    protected void onExitAction(){
        // Prevent user rapidly clicks Exit
        if (disconnected) return;
        disconnected = true;

        sC.play("button");
        sC.stopLooping();
        sC.playLooping("lobby_background",.2);
        // we're leaving on purpose: don't show ourselves a disconnect alert,
        // but DO close the connection so the opponent is notified gracefully.
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    /**
     * Mehtod to get the Piece at a given polygon
     * @param tileId
     * @return
     */
    private ImageView getPieceAtPolygon(String tileId) {
        for (Node node : boardPane.getChildren()) {
            if (node instanceof ImageView && tileId.equals(node.getUserData())) {
                return (ImageView) node;
            }
        }
        return null;
    }

    /**
     * Converts a given piece to a String so that the GameScreen can get the correct Image
     * @param piece
     * @return
     */
    private String piecetoString(Piece piece){
        StringBuilder sb = new StringBuilder();
        switch(piece.getPlayer()){
            case BLACK -> sb.append("p2");
            case WHITE -> sb.append("p1");
        }
        switch(piece.getType()){
            case PAWN -> sb.append("PawnImg");
            case KING -> sb.append("KingImg");
            case ROOK -> sb.append("RookImg");
            case QUEEN -> sb.append("QueenImg");
            case BISHOP -> sb.append("BishopImg");
            case KNIGHT -> sb.append("KnightImg");
        }
        return sb.toString();
    }

    /**
     * Method to handle dissconnects and a player leaving the game
     * @param reason
     */
    // Mid-game disconnect: opponent's window closed / connection dropped.
    // Show one alert and return to the main menu (idempotent so it fires once).
    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;
        sC.stopLooping();
        sC.playLooping("lobby_background", .2);
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason + "\n\nReturning to the main menu.",
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        alert.showAndWait();
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml", header, vS);
    }

    /**
     * Method to attack the Network to the curretn Game and send all the needede messages over the network
     * @param network
     * @param engine
     */
    public void attachHostBridge(NetworkLayer network, GameEngine engine) {
        network.addListener(new NetworkListener() {
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof ChessMessage.Input input) {
                    Platform.runLater(() -> {
                        boolean ok = engine.handleMove(input.fromId(), input.toId());
                        playSound();
                        playSound();
                        if (ok) {
                            network.send(new ChessMessage.StateUpdate(
                                    input.fromId(), input.toId(), null));
                        }
                    });
                }
                else if (msg instanceof ChessMessage.PromotionChoice choice) {
                    Platform.runLater(() -> {
                        isNetworkPromotion = true;
                        engine.promote(choice.pieceType());
                        isNetworkPromotion = false;
                    });
                }
                else if (msg instanceof ChessMessage.GameEnded endMsg) {
                    Platform.runLater(() -> {
                        network.send(endMsg);
                        showResultScreen(endMsg.reason(), endMsg.winner());
                    });
                }
                else if (msg instanceof ChessMessage.DrawOffer offer) {
                    Platform.runLater(() -> drawProposal(offer.getProposing()));
                }
                else if (msg instanceof ChessMessage.DrawDeclined declinedMsg) {
                    Platform.runLater(() -> onDrawDeclinedRemote(declinedMsg.getProposing()));
                }
            }
        });
    }

    /**
     * Method to attack the Network to the curretn Game and send all the needede messages over the network
     * @param network
     * @param engine
     */
    public void attachClientBridge(NetworkLayer network, GameEngine engine) {
        network.addListener(new NetworkListener() {
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof ChessMessage.StateUpdate update) {
                    Platform.runLater(() -> {
                        engine.handleMove(update.fromId(), update.toId());
                        playSound();
                    });
                } else if (msg instanceof ChessMessage.GameEnded endMsg) {
                    Platform.runLater(() -> {
                        showResultScreen(endMsg.reason(), endMsg.winner());
                    });
                } else if (msg instanceof ChessMessage.PromotionChoice choice) {
                    Platform.runLater(() -> {
                        isNetworkPromotion = true;
                        engine.promote(choice.pieceType());
                        isNetworkPromotion = false;
                    });
                }
                else if (msg instanceof ChessMessage.DrawOffer offer) {
                    Platform.runLater(() -> drawProposal(offer.getProposing()));
                }
                else if (msg instanceof ChessMessage.DrawDeclined declinedMsg) {
                    Platform.runLater(() -> onDrawDeclinedRemote(declinedMsg.getProposing()));
                }
            }
        });
    }

    /**
     * Method to propose a draw to the other player
     * @param proposing
     */
    private void drawProposal(int proposing){
        sC.play("button");
        try {
            Stage stage = (Stage) thisScene.getWindow();

            Stage proposalStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/HexChess/DrawProposal.fxml"));
            Parent root = loader.load();
            Scene s = new Scene(root);
            s.setFill(Color.TRANSPARENT);
            proposalStage.setScene(s);
            proposalStage.initStyle(StageStyle.TRANSPARENT);
            proposalStage.initModality(Modality.APPLICATION_MODAL);
            proposalStage.setWidth(323);
            proposalStage.setHeight(115);
            proposalStage.setX(stage.getX() + stage.getWidth() / 2 - proposalStage.getWidth() / 2);
            proposalStage.setY(stage.getY() + stage.getHeight() / 2 - proposalStage.getHeight() / 2);
            proposalStage.show();

            DrawProposal controller = loader.getController();
            controller.sendGameScreen(this);
            controller.sendPlayer(proposing);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mehtod that is called if a draw proposal is done by player 1
     */
    @FXML
    protected void onP1DrawAction() {
        // prevent opening multiple modals
        p1Draw.setDisable(true);
        sC.play("button");
        Session s = Session.current();
        if (s.network != null) {
            if (!s.isHost) return;
            s.network.send(new ChessMessage.DrawOffer(1));
        } else {
            drawProposal(1);
        }
    }

    /**
     * Mehtod that is called if player 1 resigns
     */
    @FXML
    protected void onP1ResignAction() {
        // prevent double calling end game logic
        p1Resign.setDisable(true);
        sC.play("button");
        Session s = Session.current();
        if (s.network != null && !s.isHost) return;
        resign(PlayerColor.BLACK);
    }

    /**
     * Mehtod that is called if a draw proposal is done by player 2
     */
    @FXML
    protected void onP2DrawAction() {
        // prevent opening multiple modals
        p2Draw.setDisable(true);
        sC.play("button");
        Session s = Session.current();
        if (s.network != null) {
            if (s.isHost) return;
            s.network.send(new ChessMessage.DrawOffer(2));
        } else {
            drawProposal(2);
        }
    }

    /**
     * Mehtod that is called if player 1 resigns
     */
    @FXML
    protected void onP2ResignAction() {
        // prevent double calling end game
        p2Resign.setDisable(true);
        sC.play("button");
        Session s = Session.current();
        if (s.network != null && s.isHost) return;
        resign(PlayerColor.WHITE);
    }


    /**
     * Method that is called if a player declines the draw offer
     * @param proposing
     */
    public void declined(int proposing) {
        Session s = Session.current();
        if (s.network != null) {
            // LAN: the proposer's button lives on the OTHER machine; it is
            // re-enabled there when the DrawDeclined message arrives. Re-enabling
            // here would wrongly enable the peer's button on the decliner's screen.
            s.network.send(new ChessMessage.DrawDeclined(proposing));
        } else {
            // local hot-seat: the same screen owns both buttons
            if (proposing == 1) {
                p1Draw.setDisable(false);
            } else {
                p2Draw.setDisable(false);
            }
        }
        showDeclinedToast(proposing);
    }

    /**
     * The peer declined our draw offer: re-enable our draw button and show the toast.
     * @param proposing 1 if player 1 proposed the draw, 2 if player 2 did
     */
    private void onDrawDeclinedRemote(int proposing) {
        if (proposing == 1) {
            p1Draw.setDisable(false);
        } else {
            p2Draw.setDisable(false);
        }
        showDeclinedToast(proposing);
    }

    /**
     *
     * @param proposing
     */
    private void showDeclinedToast(int proposing) {
        if (proposing == 1){
            Toast.makeText(stackPane, p2NameLabel.getText() + " has declined the Draw Proposal");
        }
        else{
            Toast.makeText(stackPane, p1NameLabel.getText() + " has declined the Draw Proposal");
        }
    }

    /**
     * Method to swap the norma Pawn Image to a Duck Image
     */
    public void p1Duck(){
        p1PawnImg.setImage(settings.getP1Pieces().get(6).getImage());
    }

    /**
     *Method to swap the normal Pawn Image to a Duck Image
     */
    public void p2Duck(){
        p2PawnImg.setImage(settings.getP2Pieces().get(6).getImage());
    }

    /**
     * Method to get the Image of a given Piece
     * @param piece
     * @return
     */
    private Image getPieceImage(Piece piece) {
        List<ImageView> pieces = (piece.getPlayer() == PlayerColor.WHITE)
                ? p1Views
                : p2Views;

        int index = switch(piece.getType()) {
            case PAWN -> 0;
            case ROOK -> 1;
            case KNIGHT -> 2;
            case BISHOP -> 3;
            case QUEEN -> 4;
            case KING -> 5;
        };
        return pieces.get(index).getImage();
    }


    /**
     * Method to highlight the currently active player
     * @param playerColor
     */
    private void highlightActivePlayer(PlayerColor playerColor) {
        if (activePlayer == PlayerColor.WHITE) {
            activePlayerLabel.setText(p1NameLabel.getText());
            activePlayerBox.getStyleClass().add("error");
            activePlayerBox.getStyleClass().remove("ready");
        }
        else {
            activePlayerLabel.setText(p2NameLabel.getText());
            activePlayerBox.getStyleClass().add("ready");
            activePlayerBox.getStyleClass().remove("error");
        }
    }
}
