/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clue.client;

import clue.GameController;
import clue.GameController.TooManyPlayersException;
import clue.MissingRoomDuringCreationException;
import clue.action.Action;
import clue.action.UnknownActionException;
import clue.card.CardType;
import clue.tile.NoSuchRoomException;
import clue.tile.NoSuchTileException;
import clue.tile.TileOccupiedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author hungb
 */
public class gameInstance {
    
    private static final int TILE_SIZE = 38;
    private int counter;
   
    private StackPane[][] board = new StackPane[25][24];
    private String notes;
    
    private HashMap<Integer, Integer> spawnlocations = new HashMap<>();
    private Player currentPlayer;
    private String remainingMoves;
    private boolean rolled;
    
    private GameController gameInterface;
    
    private HashMap<String, String> ImagePathMap = new HashMap<>();
            
    private Font avenirLarge;
    private Font avenirTitle;
    private Font avenirText;
    private final Background blackFill = new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY));
    
    private StackPane createBoard() {
        StackPane root = new StackPane();
        root.setPadding(new Insets(10, 0, 0, 0));
                
        GridPane boardPane = new GridPane();
        
        try {
            Image boardImage = new Image(new FileInputStream(ImagePathMap.get("board")));
            boardPane.setBackground(new Background(new BackgroundImage(boardImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        } catch(IOException ex) {
            System.out.println("Failed to load board texture");
        }
        
        // create base Tiles
        for (int y=0; y < 24; y++) {
            for (int x=0; x < 25; x++) {
                StackPane tilePane = new StackPane();
                Tile tile = new Tile(TILE_SIZE);
                final int coordX = x;
                final int coordY = y;
                tile.setOnMouseClicked((MouseEvent e) -> {
                    boolean moved = currentPlayer.move(coordX, coordY, board, currentPlayer);
                    System.out.println("HELLO " + counter);
                    counter++;
                });
                
                tilePane.getChildren().add(tile);
                        
                board[x][y] = tilePane;
                boardPane.add(tilePane, y, x);   
            }
        }
        
        // TODO: spawn players
        
        rolled = false;
        
        Player player1 = new Player(0, 4, "PP");
        board[5][0].getChildren().add(player1);
        currentPlayer = player1;
        
        root.getChildren().add(boardPane);
        
        return root;
    }
    
    public VBox createLeftPanel() {
        VBox leftPanelLayout = new VBox();
        leftPanelLayout.setPadding(new Insets(0, 10, 10, 10));
        
        // Notepad
        Label notepadLabel = getLabel("Notepad", avenirTitle);
        
        TextArea notepad = new TextArea();
        notepad.setPrefRowCount(20);
        notepad.setPrefColumnCount(20);
        
        // Test
        MenuItem print = new MenuItem("Print", avenirTitle);
        print.setOnMouseClicked(e -> {
            System.out.println(notepad.getText());
        });
        
        // suggestion accusation history
        Label historyLabel = getLabel("History", avenirTitle); 

        StackPane history = new StackPane();
        
        ScrollPane historyPane = new ScrollPane();
        historyPane.setPannable(false);
        historyPane.setContent(history);
        
        leftPanelLayout.getChildren().addAll(notepadLabel, notepad, print, historyLabel, historyPane);
        
        return leftPanelLayout;
    }
    
    private Label formatHistoryItem() {
        // TODO String processing
        // waiting for definite format from backend
        Label historyItem = new Label();
        
        return historyItem;
    }
    
    public GridPane createCardsDisplay() {
        GridPane cardsLayout = new GridPane();
        
        Label playerCardsLabel = getLabel("Cards", avenirTitle);
        
        int x = 0;
        int y = 0;
        System.out.println(gameInterface.getPlayer().getCards());
        for (clue.card.Card card: gameInterface.getPlayer().getCards()) {
            Image cardImage = null;
            try {
                switch (card.cardType) {
                    case PERSON:
                        cardImage = new Image(new FileInputStream(new File("./resources/character/character"+card.getid())));
                        break;
                    case WEAPON:
                        cardImage = new Image(new FileInputStream(new File("./resources/character/weapon"+card.getid())));
                        break;
                    case ROOM:
                        cardImage = new Image(new FileInputStream(new File("./resources/character/room"+card.getid())));
                        break;
                    default:
                        cardImage = new Image(new FileInputStream(new File("./resources/character/character1")));
                        break;
                }
            } catch(FileNotFoundException ex) {
                
            }
            System.out.println(cardImage);

            ImageView view = new ImageView(cardImage);
            cardsLayout.add(view, y, x);
            GridPane.setMargin(view, new Insets(0, 10, 10, 0));
            y ^= 1;
            if (y == 1) {
                x++;
            }
        }
        cardsLayout.add(playerCardsLabel, 0, 0, 2, 1);
        GridPane.setHalignment(playerCardsLabel,HPos.CENTER);
        
        return cardsLayout;
    }
    
    private VBox createPlayerControls() {        
        VBox playerControlsLayout = new VBox();
        playerControlsLayout.setAlignment(Pos.CENTER);
        
        Label remainingMovesLabel = getLabel("Roll Available", avenirTitle);

        MenuItem suggestionButton = new MenuItem("Suggestion", avenirLarge);
        suggestionButton.setActiveColor(Color.ORANGE);
        suggestionButton.setInactiveColor(Color.DARKORANGE);
        suggestionButton.setActive(false); //refresh Colour
        suggestionButton.setOnMouseClicked(e -> {
            createCardsWindow("Suggetsion", Color.ORANGE);
        });
        
        Button suggestButton = new Button("Suggestion");
        suggestButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        suggestButton.setOnAction(e -> {
            createCardsWindow("Suggestion", Color.ORANGE);
        });
        
        MenuItem accusationButton = new MenuItem("Accusation", avenirLarge);
        accusationButton.setActiveColor(Color.RED);
        accusationButton.setInactiveColor(Color.DARKRED);
        accusationButton.setActive(false);
        accusationButton.setOnMouseClicked(e -> {
            createCardsWindow("Accusation", Color.RED);
        });
        
        Button accuseButton = new Button("Accusation");
        accuseButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        accuseButton.setOnAction(e -> {
            createCardsWindow("Accusation", Color.RED);
        });
        
        MenuItem rollButton = new MenuItem("Roll", avenirLarge);
        
        rollButton.setOnMouseClicked(e -> {
            if (!rolled) {
                // waiting for gamecontroller to be finalised.
                // remainingMovesLabel.setText("Remaining Moves: " + gameInterface.roll());
                remainingMovesLabel.setText("Remaining Moves: " + gameInterface.roll());
                rolled = true;
            } else {
                Prompt alreadyRolled = new Prompt("You cannot roll");
                alreadyRolled.showAndWait();
            }
        });
        
        MenuItem endButton = new MenuItem("End Turn", avenirLarge);
        endButton.setOnMouseClicked(e -> {
            System.out.println("End Turn");
        });
        
        Button endTurnButton = new Button("End Turn");
        // Insets(top, right, bottom, left);
        playerControlsLayout.getChildren().addAll(remainingMovesLabel, suggestionButton, accusationButton, rollButton, endButton);
        
        return playerControlsLayout;        
    }
    
    private void createCardsWindow(String title, Color color) {
        selectCards cardsWindow = new selectCards();
        cardsWindow.show(title, color);
    }
    
    private BorderPane createUI() {
        BorderPane main = new BorderPane();
        main.setBackground(blackFill);
        
        main.setLeft(createLeftPanel());
        
        main.setCenter(createBoard());
        
        BorderPane rightPanel = new BorderPane();
        rightPanel.setTop(createCardsDisplay());
        rightPanel.setBottom(createPlayerControls());
        
        main.setRight(rightPanel);
        
        return main;
    }
    
    public void actionResponse(Action action) {
        switch (action.actionType) {
            case SHOWCARDS:
                
                break;
            case MOVE:
                
                break;
            case AVOIDSUGGESTIONCARD:
                
                break;
            case THROWAGAIN:
                
                break;
            case STARTTURN:
                
                break;
            case ACCUSATION:
                
                break;
        }
    }

    private Label getLabel(String text, Font font) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(font);
        return label;
    }
    
    private void initDefaultGraphics() {
        ImagePathMap.put("board", "./resources/board.png");
        
        ImagePathMap.put("character1", "./resources/Character/MissScarlet.png");
        ImagePathMap.put("character2", "./resources/Character/ColonelMustard.png");
        ImagePathMap.put("character3", "./resources/Character/MrsWhite.png");
        ImagePathMap.put("character4", "./resources/Character/MrGreen.png");
        ImagePathMap.put("character5", "./resources/Character/MrsPeacock.png");
        ImagePathMap.put("character6", "./resources/Character/ProfessorPlum.png");
        
        ImagePathMap.put("weapon1","./resources/Candlestick.png");
        ImagePathMap.put("weapon2","./resources/Dagger.png");
        ImagePathMap.put("weapon3","./resources/LeadPipe.png");
        ImagePathMap.put("weapon4","./resources/Revolver.png");
        ImagePathMap.put("weapon5","./resources/Rope.png");
        ImagePathMap.put("weapon6","./resources/Wrench.png");
        
        ImagePathMap.put("room1","./resources/Ballroom.png");
        ImagePathMap.put("room2","./resources/BillardRoom.png");
        ImagePathMap.put("room3","./resources/Conservatory.png");
        ImagePathMap.put("room4","./resources/DiningRoom.png");
        ImagePathMap.put("room5","./resources/Hall.png");
        ImagePathMap.put("room6","./resources/Kitchen.png");
        ImagePathMap.put("room7","./resources/Library.png");
        ImagePathMap.put("room8","./resources/Lounge.png");
        ImagePathMap.put("room9","./resources/Study.png");
    }
    
    private void initFonts() {
        avenirLarge = new Font(30);
        avenirTitle = new Font(20);
        avenirText = new Font(15);
        try {
            avenirLarge = Font.loadFont(new FileInputStream(new File("./resources/fonts/Avenir-Book.ttf")), 30);
            avenirTitle = Font.loadFont(new FileInputStream(new File("./resources/fonts/Avenir-Book.ttf")), 20);
            avenirText = Font.loadFont(new FileInputStream(new File("./resources/fonts/Avenir-Book.ttf")), 15);
        } catch(FileNotFoundException e) {
            
        }
    }
    
    private void initGraphics() {
        //String configPath = getClass().getResource("assets/config.properties").toExternalForm();
        try (InputStream input = new FileInputStream("resources/config.properties")) {
            Properties prop = new Properties();
            
            prop.load(input);
            System.out.println("here");
            input.close();
        } catch (IOException ex) {
            System.out.println("there");
        }
    }
        
    public void startGame(GameController gameController) {
        Stage gameStage = new Stage();
        
        gameStage.initModality(Modality.APPLICATION_MODAL);
        gameStage.setTitle("Clue");
        gameStage.setResizable(false);

        // Temp return button
        Button returnButton = new Button("Back");
        returnButton.setOnAction(e -> {
            gameStage.close();
        });
        
        gameInterface = gameController;
        
        initFonts();
        initDefaultGraphics();
        initGraphics();
        
        Scene scene = new Scene(createUI());
        gameStage.setScene(scene);
        gameStage.show();
    }
}
