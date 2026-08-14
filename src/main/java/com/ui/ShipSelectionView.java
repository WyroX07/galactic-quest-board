package com.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ShipSelectionView extends StackPane {

    private int currentPlayer = 1;
    private int totalPlayers;
    private int currentShipIndex = 0;
    private List<int[]> selections = new ArrayList<>();
    private List<String> playerNames = new ArrayList<>();
    private Set<Integer> takenShips = new HashSet<>();
    private Consumer<List<PlayerSelection>> onAllSelected;

    private TextField nameField;
    private Label titleLabel;
    private Label playerCountLabel;
    private ImageView shipImageView;
    private Label shipNameLabel;
    private Button btnLeft;
    private Button btnRight;
    private Button btnChoose;
    private VBox playerListBox;

    private final String[] shipNames = {"X-WING", "Y-WING", "TIE FIGHTER", "MILLENNIUM FALCON"};
    private final Image[] shipImages = new Image[4];

    public ShipSelectionView(int totalPlayers, Consumer<List<PlayerSelection>> onAllSelected) {
        this.totalPlayers = totalPlayers;
        this.onAllSelected = onAllSelected;

        for (int i = 0; i < 4; i++) {
            shipImages[i] = new Image(
                    getClass().getResourceAsStream("/img/ships/ship" + (i + 1) + ".png")
            );
        }

        String bgUrl = getClass().getResource("/img/ship_selection_bg.jpg").toExternalForm();
        this.setStyle("-fx-background-image: url('" + bgUrl + "'); "
                + "-fx-background-size: cover; "
                + "-fx-background-position: center center;");

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setMouseTransparent(true);


        // Left panel — ship carousel

        titleLabel = new Label("HANGAR — SHIP SELECTION");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#4ECDC4"));

        playerCountLabel = new Label("PILOT 1/" + totalPlayers);
        playerCountLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        playerCountLabel.setTextFill(Color.web("#888888"));

        Line headerLine = new Line(0, 0, 450, 0);
        headerLine.setStroke(Color.web("#4ECDC4"));
        headerLine.setStrokeWidth(1);
        headerLine.setOpacity(0.4);

        VBox header = new VBox(5, titleLabel, playerCountLabel, headerLine);
        header.setAlignment(Pos.CENTER);

        StackPane shipPlatform = new StackPane();
        shipImageView = new ImageView(shipImages[0]);
        shipImageView.setFitWidth(220);
        shipImageView.setFitHeight(220);
        shipImageView.setPreserveRatio(true);

        shipPlatform.getChildren().add(shipImageView);

        btnLeft = createArrowButton("◀");
        btnLeft.setOnAction(e -> navigate(-1));

        btnRight = createArrowButton("▶");
        btnRight.setOnAction(e -> navigate(1));

        HBox carousel = new HBox(20, btnLeft, shipPlatform, btnRight);
        carousel.setAlignment(Pos.CENTER);

        shipNameLabel = new Label(shipNames[0]);
        shipNameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        shipNameLabel.setTextFill(Color.WHITE);

        VBox shipInfo = new VBox(4, shipNameLabel);
        shipInfo.setAlignment(Pos.CENTER);

        Label pilotLabel = new Label();

        nameField = new TextField();
        nameField.setPromptText("Enter your nickname here...");
        nameField.setMaxWidth(280);
        nameField.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px; -fx-padding: 10px; "
                + "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; "
                + "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(78,205,196,0.4); "
                + "-fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        btnChoose = new Button("DEPLOY THIS SHIP");
        btnChoose.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        btnChoose.setStyle("-fx-background-color: #4ECDC4; -fx-text-fill: #0a0a0a; "
                + "-fx-padding: 12 40; -fx-cursor: hand; -fx-background-radius: 4;");
        btnChoose.setOnAction(e -> selectShip());

        VBox bottomSection = new VBox(8, pilotLabel, nameField, btnChoose);
        bottomSection.setAlignment(Pos.CENTER);

        VBox leftSide = new VBox(10, header, carousel, shipInfo, bottomSection);
        leftSide.setAlignment(Pos.TOP_CENTER);
        leftSide.setPadding(new Insets(280, 20, 20, 275));


        // Right panel — player list

        Label crewTitle = new Label("SQUADRON");
        crewTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        crewTitle.setTextFill(Color.web("#4ECDC4"));

        Line crewLine = new Line(0, 0, 250, 0);
        crewLine.setStroke(Color.web("#4ECDC4"));
        crewLine.setStrokeWidth(1);
        crewLine.setOpacity(0.4);

        playerListBox = new VBox(10);
        playerListBox.setAlignment(Pos.TOP_CENTER);

        for (int i = 1; i <= totalPlayers; i++) {
            playerListBox.getChildren().add(createEmptyPlayerSlot(i));
        }

        VBox rightSide = new VBox(15, crewTitle, crewLine, playerListBox);
        rightSide.setAlignment(Pos.TOP_CENTER);
        rightSide.setPadding(new Insets(40, 30, 40, 20));
        rightSide.setMinWidth(280);
        rightSide.setMaxWidth(280);
        rightSide.setStyle("-fx-background-color: rgba(0,0,0,0.4); "
                + "-fx-border-color: rgba(78,205,196,0.2); -fx-border-width: 0 0 0 1;");

        // Main layout
        HBox mainLayout = new HBox(leftSide, rightSide);
        HBox.setHgrow(leftSide, Priority.ALWAYS);
        mainLayout.setAlignment(Pos.CENTER);

        this.getChildren().addAll(overlay, mainLayout);

        findNextAvailableShip();
    }

    private HBox createEmptyPlayerSlot(int playerNumber) {
        Label numLabel = new Label(String.format("%02d", playerNumber));
        numLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        numLabel.setTextFill(Color.web("#4ECDC4"));

        Label nameLabel = new Label("Waiting...");
        nameLabel.setFont(Font.font("Consolas", FontWeight.NORMAL, 13));
        nameLabel.setTextFill(Color.web("#555555"));

        HBox slot = new HBox(15, numLabel, nameLabel);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setPadding(new Insets(10, 15, 10, 15));
        slot.setStyle("-fx-background-color: rgba(255,255,255,0.03); "
                + "-fx-border-color: rgba(78,205,196,0.15); -fx-border-radius: 4; "
                + "-fx-background-radius: 4;");
        return slot;
    }

    private HBox createFilledPlayerSlot(int playerNumber, String name, int shipNumber) {
        Label numLabel = new Label(String.format("%02d", playerNumber));
        numLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        numLabel.setTextFill(Color.web("#4ECDC4"));

        ImageView miniShip = new ImageView(shipImages[shipNumber - 1]);
        miniShip.setFitWidth(30);
        miniShip.setFitHeight(30);
        miniShip.setPreserveRatio(true);

        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.WHITE);

        Label shipLabel = new Label(shipNames[shipNumber - 1]);
        shipLabel.setFont(Font.font("Consolas", FontWeight.NORMAL, 10));
        shipLabel.setTextFill(Color.web("#888888"));

        info.getChildren().addAll(nameLabel, shipLabel);

        HBox slot = new HBox(12, numLabel, miniShip, info);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setPadding(new Insets(10, 15, 10, 15));
        slot.setStyle("-fx-background-color: rgba(78,205,196,0.1); "
                + "-fx-border-color: rgba(78,205,196,0.4); -fx-border-radius: 4; "
                + "-fx-background-radius: 4;");
        return slot;
    }

    private Button createArrowButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: rgba(78,205,196,0.15); -fx-text-fill: #4ECDC4; "
                + "-fx-font-size: 24px; -fx-padding: 15 20; -fx-cursor: hand; "
                + "-fx-background-radius: 4; -fx-border-color: rgba(78,205,196,0.3); -fx-border-radius: 4;");
        return btn;
    }

    private void navigate(int direction) {
        int next = currentShipIndex + direction;
        if (next < 0) next = 3;
        if (next > 3) next = 0;

        currentShipIndex = next;

        int attempts = 0;
        while (takenShips.contains(currentShipIndex + 1) && attempts < 4) {
            currentShipIndex += direction > 0 ? 1 : -1;
            if (currentShipIndex < 0) currentShipIndex = 3;
            if (currentShipIndex > 3) currentShipIndex = 0;
            attempts++;
        }

        updateDisplay();
    }

    private void findNextAvailableShip() {
        int attempts = 0;
        while (takenShips.contains(currentShipIndex + 1) && attempts < 4) {
            currentShipIndex++;
            if (currentShipIndex > 3) currentShipIndex = 0;
            attempts++;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        shipImageView.setImage(shipImages[currentShipIndex]);
        shipNameLabel.setText(shipNames[currentShipIndex]);
    }

    private void selectShip() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            nameField.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px; -fx-padding: 10px; "
                    + "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; "
                    + "-fx-prompt-text-fill: rgba(255,255,255,0.5); "
                    + "-fx-border-color: #ff4444; -fx-border-width: 2; -fx-background-radius: 4; -fx-border-radius: 4;");
            nameField.setPromptText("NICKNAME REQUIRED !");
            return;
        }

        nameField.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px; -fx-padding: 10px; "
                + "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; "
                + "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(78,205,196,0.4); "
                + "-fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        int shipNumber = currentShipIndex + 1;
        playerNames.add(name);
        selections.add(new int[]{shipNumber});
        takenShips.add(shipNumber);

        playerListBox.getChildren().set(
                currentPlayer - 1,
                createFilledPlayerSlot(currentPlayer, name, shipNumber)
        );

        currentPlayer++;

        if (currentPlayer > totalPlayers) {
            List<PlayerSelection> result = new ArrayList<>();
            for (int i = 0; i < selections.size(); i++) {
                result.add(new PlayerSelection(playerNames.get(i), selections.get(i)[0]));
            }
            onAllSelected.accept(result);
        } else {
            titleLabel.setText("HANGAR — SHIP SELECTION");
            playerCountLabel.setText("PILOT " + currentPlayer + "/" + totalPlayers);
            nameField.clear();
            nameField.setPromptText("Enter your nickname here...");
            findNextAvailableShip();
        }
    }

    public static class PlayerSelection {
        public final String name;
        public final int shipNumber;

        public PlayerSelection(String name, int shipNumber) {
            this.name = name;
            this.shipNumber = shipNumber;
        }
    }
}