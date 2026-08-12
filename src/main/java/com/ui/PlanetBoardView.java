package com.ui;

import com.demoMapProjet.model.BoardDefinition;
import com.demoMapProjet.model.PlayerToken;
import com.demoMapProjet.model.QuestionDefinition;
import com.demoMapProjet.model.TileDefinition;
import com.service.QuestionService;
import com.service.strategy.AllInTileQuestionStrategy;
import com.service.strategy.TileQuestionStrategy;
import com.service.strategy.TileQuestionStrategyFactory;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class PlanetBoardView extends Pane {

    private AnswerListener answerListener;
    private final BoardDefinition board;
    private final QuestionService questionService;
    private StackPane gameRoot;
    private final TileQuestionStrategyFactory strategyFactory = new TileQuestionStrategyFactory();
    private final Map<Integer, StackPane> tileNodesById = new HashMap<>();
    private final List<Line> links = new ArrayList<>();
    private final Map<PlayerToken, ImageView> tokenViews = new HashMap<>();
    private javafx.animation.Timeline currentAnimation;
    private Runnable onAnimationFinished;

    /** Returns true when the given player has completed their quest (set by GameController). */
    private Predicate<PlayerToken> questCompletedChecker;

    /** Called by PlanetBoardView when a player wins the final assault. */
    private Runnable onFinalAssaultWin;

    public PlanetBoardView(BoardDefinition board, QuestionService questionService) {
        this.board = board;
        this.questionService = questionService;

        for (TileDefinition tile : board.getTiles()) {
            StackPane tileNode = createTileNode(tile, board.getTileSize());
            tileNode.setUserData(tile);
            // Tiles are invisible; the map image already shows the visual track
            tileNode.setOpacity(0);
            tileNode.setMouseTransparent(true);

            tileNodesById.put(tile.getId(), tileNode);
            getChildren().add(tileNode);
        }
    }

    public void setGameRoot(StackPane gameRoot) {
        this.gameRoot = gameRoot;
    }

    public void setQuestCompletedChecker(Predicate<PlayerToken> checker) {
        this.questCompletedChecker = checker;
    }

    public void setOnFinalAssaultWin(Runnable callback) {
        this.onFinalAssaultWin = callback;
    }

    private boolean isStartTile(TileDefinition tile) {
        return tile != null && "START".equalsIgnoreCase(tile.getType());
    }


    public void ajouterToken(PlayerToken token) {
        Image shipImage = new Image(
                getClass().getResourceAsStream("/img/ships/ship" + token.getShipNumber() + ".png")
        );
        ImageView view = new ImageView(shipImage);
        view.setFitWidth(70);
        view.setFitHeight(70);
        view.setPreserveRatio(true);
        view.setMouseTransparent(true);
        javafx.scene.effect.DropShadow outline = new javafx.scene.effect.DropShadow();
        outline.setColor(javafx.scene.paint.Color.BLACK);
        outline.setRadius(5);
        outline.setSpread(0.8);
        view.setEffect(outline);

        tokenViews.put(token, view);
        getChildren().add(view);
    }

    public void mooveToken(PlayerToken token) {
        askQuestionForCurrentTile(token);
    }

    public void askQuestionForCurrentTile(PlayerToken token) {
        TileDefinition currentTile = getCurrentTile(token);

        // ── Final assault: player on START and quest already completed ──────
        if (isStartTile(currentTile)
                && questCompletedChecker != null
                && questCompletedChecker.test(token)) {
            showFinalAssault(token);
            return;
        }

        System.out.println("Tile: type=" + currentTile.getType()
                + " / theme=" + currentTile.getTheme());

        TileQuestionStrategy strategy = strategyFactory.getStrategy(currentTile);

        if (isAllInTile(currentTile)) {
            // ALL_IN: use extended listener to get the chosen level for move computation
            strategy.askQuestion(currentTile, questionService, gameRoot,token.getPlayerName(),
                    new AllInTileQuestionStrategy.AllInAnswerListener() {
                        @Override
                        public void onAllInAnswer(QuestionDefinition question,
                                                  boolean isCorrect,
                                                  int chosenLevel) {
                            applyAllInRule(token, isCorrect, chosenLevel);
                            if (answerListener != null) {
                                answerListener.onAnswer(question, isCorrect);
                            }
                        }
                    });

        } else {
            strategy.askQuestion(currentTile, questionService, gameRoot, token.getPlayerName(), (question, isCorrect) -> {
                if (isVaderTile(currentTile)) {
                    applyVaderRule(token, isCorrect);
                }
                if (answerListener != null) {
                    answerListener.onAnswer(question, isCorrect);
                }
            });
        }
    }

    public TileDefinition getCurrentTile(PlayerToken token) {
        return getOrderedTiles().stream()
                .filter(tile -> tile.getId() == token.getCurrentPosition())
                .findFirst()
                .orElse(getOrderedTiles().get(0));
    }

    private List<TileDefinition> getOrderedTiles() {
        return board.getTiles().stream()
                .sorted(Comparator.comparingInt(TileDefinition::getId))
                .toList();
    }

    private boolean isVaderTile(TileDefinition tile) {
        return tile != null
                && tile.getType() != null
                && ("DARK_VADOR".equalsIgnoreCase(tile.getType()) || "VADER".equalsIgnoreCase(tile.getType()));
    }

    private boolean isAllInTile(TileDefinition tile) {
        return tile != null
                && tile.getType() != null
                && "ALL_IN".equalsIgnoreCase(tile.getType());
    }

    private void applyVaderRule(PlayerToken token, boolean isCorrect) {
        if (isCorrect) {
            moveTokenBySteps(token, 4);
        } else {
            moveTokenToStart(token);
        }
    }

    /**
     * Applies ALL_IN move rules after the player has answered.
     *
     * Correct answer  -> advance by (chosenLevel + 1) tiles.
     * Wrong answer    -> move back by chosenLevel tiles.
     *
     * @param token       the player token to move
     * @param isCorrect   whether the answer was correct
     * @param chosenLevel the difficulty level the player selected (1-4)
     */
    private void applyAllInRule(PlayerToken token, boolean isCorrect, int chosenLevel) {
        if (isCorrect) {
            int advance = chosenLevel + 1;
            moveTokenBySteps(token, advance);
            System.out.println("[ALL-IN] Correct! Player advances " + advance + " tiles.");
        } else {
            int retreat = chosenLevel;
            moveTokenBySteps(token, -retreat);
            System.out.println("[ALL-IN] Wrong! Player moves back " + retreat + " tiles.");
        }
    }

    // ── Final Assault overlay ─────────────────────────────────────────────────

    /**
     * Shows a theme-selector card for the Final Assault.
     * The player picks a theme; a difficulty-4 question is then asked.
     * Correct → win. Wrong → turn ends normally.
     */
    private void showFinalAssault(PlayerToken token) {
        if (gameRoot == null) return;
        String playerName = token.getPlayerName();

        // Blur background
        for (var child : gameRoot.getChildren()) {
            child.setEffect(new GaussianBlur(12));
        }

        // Semi-transparent overlay
        Rectangle dimOverlay = new Rectangle();
        dimOverlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 20, 0.65));
        dimOverlay.widthProperty().bind(gameRoot.widthProperty());
        dimOverlay.heightProperty().bind(gameRoot.heightProperty());
        dimOverlay.setMouseTransparent(false);

        double cardWidth  = 780;
        double cardHeight = 570;
        StackPane cardContainer = new StackPane();
        cardContainer.setMaxSize(cardWidth, cardHeight);
        cardContainer.setMinSize(cardWidth, cardHeight);

        // Yellow card background (final battle theme)
        ImageView cardBg = new ImageView();
        try {
            cardBg.setImage(new Image(
                    getClass().getResourceAsStream("/img/cardQuestionYellow.png")));
        } catch (Exception ignored) {}
        cardBg.setFitWidth(cardWidth);
        cardBg.setFitHeight(cardHeight);
        cardBg.setPreserveRatio(false);

        Label titleLabel = new Label("⚡ ASSAUT FINAL — " + playerName.toUpperCase() + " !");
        titleLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 6, 0.6, 0, 2);"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(620);
        titleLabel.setAlignment(Pos.CENTER);

        Label subLabel = new Label(
                "Quête accomplie ! Choisissez votre thème  •  Difficulté 4  •  Correct = VICTOIRE !"
        );
        subLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);" +
                        "-fx-effect: dropshadow(gaussian, black, 3, 0.4, 0, 1);"
        );
        subLabel.setWrapText(true);
        subLabel.setMaxWidth(620);
        subLabel.setAlignment(Pos.CENTER);

        // Theme buttons
        HBox buttonsBox = new HBox(14);
        buttonsBox.setAlignment(Pos.CENTER);

        String[] themeLabels = {"🔵\nTech / IT", "🟢\nTourism\n& World", "🟠\nEntertain-\nment", "⭐\nStar Wars"};
        String[] themeKeys   = {"IT & Programming", "Tourism & Travel", "Entertainment", "Star Wars"};
        String[] bgColors    = {"#4CC9F0", "#80ED99", "#F4A261", "#FFD60A"};

        for (int i = 0; i < 4; i++) {
            final String themeKey = themeKeys[i];
            Button btn = buildThemeBtn(themeLabels[i], bgColors[i]);

            btn.setOnAction(e -> {
                gameRoot.getChildren().remove(cardContainer);
                gameRoot.getChildren().remove(dimOverlay);
                for (var child : gameRoot.getChildren()) child.setEffect(null);

                QuestionDefinition question = questionService.getQuestionForTheme(themeKey, 4);
                System.out.println("[FINAL ASSAULT] theme=" + themeKey
                        + " / question=" + (question != null ? question.getTitle() : "null"));

                QuestionCardView.show(question, gameRoot, playerName, isCorrect -> {
                    if (isCorrect) {
                        if (onFinalAssaultWin != null) onFinalAssaultWin.run();
                    } else {
                        // Wrong → end turn without moving (player stays on START)
                        if (answerListener != null) answerListener.onAnswer(question, false);
                    }
                });
            });

            buttonsBox.getChildren().add(btn);
        }

        VBox content = new VBox(24, titleLabel, subLabel, buttonsBox);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(70, 55, 50, 55));
        content.setMaxWidth(cardWidth);

        // Close (X) button with "Retour à la sélection" back button
        Button closeBtn = new Button("X");
        closeBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-font-size: 14px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20;" +
                        "-fx-min-width: 28px; -fx-min-height: 28px; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> {
            cardContainer.setVisible(false);
            dimOverlay.setVisible(false);
            for (var child : gameRoot.getChildren()) {
                if (child != cardContainer && child != dimOverlay) child.setEffect(null);
            }

            Button backBtn = new Button("Retour à la sélection");
            backBtn.setStyle(
                    "-fx-background-color: #4ECDC4; -fx-text-fill: white; -fx-font-size: 16px;" +
                            "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8;" +
                            "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);"
            );
            StackPane.setAlignment(backBtn, Pos.BOTTOM_CENTER);
            StackPane.setMargin(backBtn, new Insets(0, 0, 30, 0));
            gameRoot.getChildren().add(backBtn);

            backBtn.setOnAction(ev -> {
                gameRoot.getChildren().remove(backBtn);
                cardContainer.setVisible(true);
                dimOverlay.setVisible(true);
                for (var child : gameRoot.getChildren()) {
                    if (child != cardContainer && child != dimOverlay)
                        child.setEffect(new GaussianBlur(12));
                }
            });
        });

        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane innerCard = new StackPane(cardBg, content, closeBtn);
        StackPane.setMargin(closeBtn, new Insets(10, 10, 0, 0));

        cardContainer.getChildren().add(innerCard);
        gameRoot.getChildren().add(dimOverlay);
        gameRoot.getChildren().add(cardContainer);
    }

    private static Button buildThemeBtn(String labelText, String bgColor) {
        Button btn = new Button(labelText);
        String base =
                "-fx-background-color: " + bgColor + "; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12;" +
                        "-fx-padding: 12 16 12 16; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.3, 0, 2);" +
                        "-fx-alignment: center; -fx-text-alignment: center;";
        String hover =
                "-fx-background-color: derive(" + bgColor + ", -15%); -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12;" +
                        "-fx-padding: 12 16 12 16; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0.4, 0, 3);" +
                        "-fx-alignment: center; -fx-text-alignment: center;" +
                        "-fx-scale-x: 1.05; -fx-scale-y: 1.05;";
        btn.setStyle(base);
        btn.setPrefWidth(130);
        btn.setPrefHeight(95);
        btn.setWrapText(true);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
        return btn;
    }

    public void setOnAnimationFinished(Runnable callback) {
        this.onAnimationFinished = callback;
    }

    public void moveTokenBySteps(PlayerToken token, int steps) {
        if (steps == 0) return;

        if (currentAnimation != null) {
            currentAnimation.stop();
        }
        List<TileDefinition> orderedTiles = getOrderedTiles();
        int size = orderedTiles.size();
        int direction = steps > 0 ? 1 : -1;
        int absSteps = Math.abs(steps);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline();

        for (int i = 1; i <= absSteps; i++) {
            final int step = i;
            javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * 400),
                    event -> {
                        int newPos = Math.floorMod(token.getCurrentPosition() + direction, size);
                        token.setCurrentPosition(newPos);
                        requestLayout();
                    }
            );
            timeline.getKeyFrames().add(kf);
        }
        currentAnimation = timeline;
        timeline.setOnFinished(e -> {
            currentAnimation = null;
            if (onAnimationFinished != null) onAnimationFinished.run();
        });
        timeline.play();
    }

    private void moveTokenToStart(PlayerToken token) {
        TileDefinition startTile = getOrderedTiles().stream()
                .filter(tile -> "START".equalsIgnoreCase(tile.getType()))
                .findFirst()
                .orElse(getOrderedTiles().get(0));

        token.setCurrentPosition(startTile.getId());
        requestLayout();
    }

    private void buildLinksFollowingIds() {
        List<TileDefinition> ordered = getOrderedTiles();

        for (int i = 0; i < ordered.size() - 1; i++) {
            TileDefinition a = ordered.get(i);
            TileDefinition b = ordered.get(i + 1);

            Line line = new Line();
            line.setStroke(Color.WHITE);
            line.setStrokeWidth(4);
            line.setMouseTransparent(true);

            getChildren().add(0, line);
            links.add(line);

            line.getProperties().put("fromId", a.getId());
            line.getProperties().put("toId", b.getId());
        }

        TileDefinition first = ordered.get(0);
        TileDefinition last = ordered.get(ordered.size() - 1);

        Line closingLine = new Line();
        closingLine.setStroke(Color.WHITE);
        closingLine.setStrokeWidth(4);
        closingLine.setMouseTransparent(true);

        closingLine.getProperties().put("fromId", last.getId());
        closingLine.getProperties().put("toId", first.getId());

        getChildren().add(0, closingLine);
        links.add(closingLine);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();

        // rx/ry are relative to the full panel size
        for (var node : getChildren()) {
            if (!(node instanceof StackPane tileNode)) continue;

            TileDefinition t = (TileDefinition) tileNode.getUserData();
            if (t == null) continue;

            double ts = board.getTileSize();
            double x = t.getRx() * w - ts / 2.0;
            double y = t.getRy() * h - ts / 2.0;

            tileNode.resizeRelocate(x, y, ts, ts);
        }

        updateLinks();

        int i = 0;
        for (Map.Entry<PlayerToken, ImageView> entry : tokenViews.entrySet()) {
            PlayerToken token = entry.getKey();
            ImageView view = entry.getValue();

            TileDefinition tile = board.getTiles().stream()
                    .filter(t -> t.getId() == token.getCurrentPosition())
                    .findFirst()
                    .orElse(board.getTiles().get(0));

            double x = tile.getRx() * w;
            double y = tile.getRy() * h;

            double offset = (i - (tokenViews.size() - 1) / 2.0) * 20;

            view.setX(x - view.getFitWidth() / 2 + offset);
            view.setY(y - view.getFitHeight() / 2);
            i++;
        }
    }

    private void updateLinks() {
        for (Line line : links) {
            Integer fromId = (Integer) line.getProperties().get("fromId");
            Integer toId = (Integer) line.getProperties().get("toId");

            StackPane fromNode = tileNodesById.get(fromId);
            StackPane toNode = tileNodesById.get(toId);

            if (fromNode == null || toNode == null) {
                line.setVisible(false);
                continue;
            }

            double x1 = fromNode.getLayoutX() + fromNode.getWidth() / 2.0;
            double y1 = fromNode.getLayoutY() + fromNode.getHeight() / 2.0;

            double x2 = toNode.getLayoutX() + toNode.getWidth() / 2.0;
            double y2 = toNode.getLayoutY() + toNode.getHeight() / 2.0;

            line.setStartX(x1);
            line.setStartY(y1);
            line.setEndX(x2);
            line.setEndY(y2);
            line.setVisible(true);
        }
    }

    private StackPane createTileNode(TileDefinition tile, int tileSize) {
        double r = tileSize / 2.0;

        Circle circle = new Circle(r);
        circle.setFill(colorFor(tile));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(3);
        circle.setMouseTransparent(true);

        DropShadow shadow = new DropShadow(6, Color.rgb(0, 0, 0, 0.35));
        shadow.setOffsetY(2);
        circle.setEffect(shadow);

        StackPane cell = new StackPane(circle);
        cell.setPrefSize(tileSize, tileSize);
        cell.setMinSize(tileSize, tileSize);
        cell.setMaxSize(tileSize, tileSize);

        return cell;
    }

    private Color colorFor(TileDefinition tile) {
        if (tile == null || tile.getType() == null) {
            return Color.WHITE;
        }

        if ("START".equalsIgnoreCase(tile.getType())) return Color.web("#D6D6D6");
        if ("ALL_IN".equalsIgnoreCase(tile.getType())) return Color.web("#C77DFF");
        if ("DARK_VADOR".equalsIgnoreCase(tile.getType()) || "VADER".equalsIgnoreCase(tile.getType())) return Color.web("#9B2226");

        if ("THEME".equalsIgnoreCase(tile.getType())) {
            String theme = tile.getTheme();
            if (theme == null) {
                return Color.WHITE;
            }

            return switch (theme.toUpperCase()) {
                case "ORANGE" -> Color.web("#F4A261");
                case "BLUE" -> Color.web("#4CC9F0");
                case "GREEN" -> Color.web("#80ED99");
                case "YELLOW" -> Color.web("#FFD60A");
                default -> Color.WHITE;
            };
        }
        return Color.WHITE;
    }

    public void avancerToken(PlayerToken token) {
        int currentPosition = token.getCurrentPosition();
        List<TileDefinition> ordered = getOrderedTiles();

        int nextPosition = (currentPosition + 1) % ordered.size();
        token.setCurrentPosition(nextPosition);
        requestLayout();
    }

    public interface AnswerListener {
        void onAnswer(QuestionDefinition question, boolean isCorrect);
    }

    public void setAnswerListener(AnswerListener answerListener) {
        this.answerListener = answerListener;
    }
}