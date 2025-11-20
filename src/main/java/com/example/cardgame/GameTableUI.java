package com.example.cardgame;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.*;

public class GameTableUI {

    private final DeckService deckService = new DeckService();
    private final AudioClip drawSound = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/sounds/draw.mp3")).toExternalForm()
    );

    private TurnManager turnManager;

    private final BorderPane root = new BorderPane();
    private final ImageView cardImage = new ImageView();
    private final Label ruleLabel = new Label();
    private final Random random = new Random();

    private final ImageView botTop = new ImageView();
    private final ImageView botLeft = new ImageView();
    private final ImageView botRight = new ImageView();
    private final ImageView botBottom = new ImageView();
    private final Button drawButton = new Button("Следваща карта");

    private final Map<ImageView, Timeline> runningAnimations = new HashMap<>();
    private final Map<Integer, Double> drunkLevels = new HashMap<>();
    private final Map<Integer, ProgressBar> drunkBars = new HashMap<>();



    public GameTableUI() {
        drunkLevels.put(1, 0.0);
        drunkLevels.put(2, 0.0);
        drunkLevels.put(3, 0.0);
        drunkLevels.put(4, 0.0);

        setupCardArea();
        setupBots();
        setupDrunkBars();   // <-- НОВО
        setupDrawButton();

        turnManager = new TurnManager(this);
        turnManager.start();

        flipCard();
    }


    private void setupCardArea() {
        cardImage.setFitWidth(260);
        cardImage.setFitHeight(360);
        cardImage.setPreserveRatio(true);
        cardImage.setRotationAxis(Rotate.Y_AXIS);

        ruleLabel.setFont(new Font(18));
        ruleLabel.setWrapText(true);
        ruleLabel.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(10, cardImage, ruleLabel);
        centerBox.setAlignment(Pos.CENTER);
        root.setCenter(centerBox);
    }


    private void setupBots() {
        styleBot(botTop);
        styleBot(botLeft);
        styleBot(botRight);
        styleBot(botBottom);

        HBox topBox = new HBox(botTop);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        VBox leftBox = new VBox(botLeft);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        root.setLeft(leftBox);

        VBox rightBox = new VBox(botRight);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        root.setRight(rightBox);

        HBox bottomPlaceholder = new HBox(botBottom);
        bottomPlaceholder.setAlignment(Pos.CENTER);
        root.setBottom(bottomPlaceholder);

        // стартови idle анимации за всички ботове
        playAnimation(botTop, "/bots/player1", "idle", 2, 500);
        playAnimation(botLeft, "/bots/player2", "idle", 2, 500);
        playAnimation(botRight, "/bots/player3", "idle", 2, 500);
        playAnimation(botBottom, "/bots/player4", "idle", 2, 500);

        // демонстрация: след 3 секунди преминаване към walk
        new Timeline(new KeyFrame(Duration.seconds(3), e -> playAnimation(botTop, "/bots/player1", "walk", 2, 200))).play();
        new Timeline(new KeyFrame(Duration.seconds(3), e -> playAnimation(botLeft, "/bots/player2", "walk", 2, 200))).play();
        new Timeline(new KeyFrame(Duration.seconds(3), e -> playAnimation(botRight, "/bots/player3", "walk", 2, 200))).play();
        new Timeline(new KeyFrame(Duration.seconds(3), e -> playAnimation(botBottom, "/bots/player4", "walk", 2, 200))).play();
    }

    private void setupDrunkBars() {

        ProgressBar barTop = new ProgressBar(0);
        ProgressBar barLeft = new ProgressBar(0);
        ProgressBar barRight = new ProgressBar(0);
        ProgressBar barBottom = new ProgressBar(0);

        barTop.setPrefWidth(80);
        barLeft.setPrefWidth(80);
        barRight.setPrefWidth(80);
        barBottom.setPrefWidth(120);

        // правилно свързване с ID
        drunkBars.put(1, barTop);     // бот 1 – горе
        drunkBars.put(2, barLeft);    // бот 2 – ляво
        drunkBars.put(3, barRight);   // бот 3 – дясно
        drunkBars.put(4, barBottom);  // играч – долу

        // root.getTop() → HBox
        HBox topBox = (HBox) root.getTop();
        VBox topContainer = new VBox(botTop, barTop);
        topContainer.setAlignment(Pos.CENTER);
        topBox.getChildren().setAll(topContainer);

        // root.getLeft() → VBox
        VBox leftBox = (VBox) root.getLeft();
        HBox leftContainer = new HBox(botLeft, barLeft);
        leftContainer.setAlignment(Pos.CENTER_LEFT);
        leftBox.getChildren().setAll(leftContainer);

        // root.getRight() → VBox
        VBox rightBox = (VBox) root.getRight();
        HBox rightContainer = new HBox(barRight, botRight); // обратен ред – първо бар, после бот
        rightContainer.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().setAll(rightContainer);

        // долу → HBox (botBottom + бутона)
        HBox bottomBox = (HBox) root.getBottom();
        VBox bottomContainer = new VBox(botBottom, barBottom);
        bottomContainer.setAlignment(Pos.CENTER);
        bottomBox.getChildren().add(bottomContainer);
    }

    private void setupDrawButton() {
        drawButton.setFont(new Font(20));

        drawButton.setOnAction(e -> {
            drawButton.setDisable(true);

            // ботовете анимират walk
            playAnimation(botTop, "/bots/player1", "walk", 2, 200);
            playAnimation(botLeft, "/bots/player2", "walk", 2, 200);
            playAnimation(botRight, "/bots/player3", "walk", 2, 200);
            playAnimation(botBottom, "/bots/player4", "walk", 2, 200);

            // играч тегли карта
            flipCardWithBack();

            // връщаме анимация idle след 1 секунда
            new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                playAnimation(botTop, "/bots/player1", "idle", 2, 500);
                playAnimation(botLeft, "/bots/player2", "idle", 2, 500);
                playAnimation(botRight, "/bots/player3", "idle", 2, 500);
                playAnimation(botBottom, "/bots/player4", "idle", 2, 500);
            })).play();

            // преминаване към следващия играч
            turnManager.nextTurn();
        });

        HBox bottomBox = new HBox(10, botBottom, drawButton);
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);
    }


    private void playAnimation(ImageView bot, String playerPath, String anim, int frameCountHint, int speedMs) {
        Timeline prev = runningAnimations.get(bot);
        if (prev != null) prev.stop();

        List<Image> framesList = new ArrayList<>();
        for (int i = 0; i < frameCountHint; i++) {
            String path = playerPath + "/" + anim + "/" + i + ".png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.out.println("Missing resource: " + path);
                continue;
            }
            framesList.add(new Image(is, 0, 0, true, false));
        }

        if (framesList.isEmpty()) {
            System.out.println("No frames loaded for " + playerPath + "/" + anim + " — aborting animation.");
            return;
        }

        Timeline timeline = new Timeline();
        Duration frameDuration = Duration.millis(Math.max(1, speedMs));

        for (int i = 0; i < framesList.size(); i++) {
            final Image frame = framesList.get(i);
            timeline.getKeyFrames().add(new KeyFrame(frameDuration.multiply(i), ev -> bot.setImage(frame)));
        }
        timeline.getKeyFrames().add(new KeyFrame(frameDuration.multiply(framesList.size()), ev -> {}));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        runningAnimations.put(bot, timeline);

        bot.setFitWidth(64);
        bot.setFitHeight(64);
        bot.setPreserveRatio(true);
        bot.setSmooth(false);
    }


    private void styleBot(ImageView bot) {
        bot.setFitWidth(64);
        bot.setFitHeight(64);
        bot.setPreserveRatio(true);
        bot.setSmooth(false);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setColor(Color.BLACK);
        bot.setEffect(shadow);
    }


    public Pane getRoot() {
        return root;
    }


    private void flipCard() {
        Card c = deckService.drawCard();
        if (c == null) {
            ruleLabel.setText("Няма повече карти!");
            return;
        }

        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cards/" + c.getImagePath())));
        ruleLabel.setText(c.getRule());

        cardImage.setTranslateX(-300);
        cardImage.setTranslateY(-300);
        cardImage.setRotate(0);
        cardImage.setRotationAxis(Rotate.Y_AXIS);

        TranslateTransition move = new TranslateTransition(Duration.millis(2000), cardImage);
        move.setToX(0);
        move.setToY(0);

        RotateTransition flip1 = new RotateTransition(Duration.millis(1000), cardImage);
        flip1.setFromAngle(0);
        flip1.setToAngle(90);
        flip1.setOnFinished(e -> cardImage.setImage(img));

        RotateTransition flip2 = new RotateTransition(Duration.millis(1000), cardImage);
        flip2.setFromAngle(90);
        flip2.setToAngle(0);

        SequentialTransition seq = new SequentialTransition(move, flip1, flip2);
        drawSound.play();
        seq.play();
    }

    private void shakeBot(ImageView bot, double intensity) {

        // intensity = 0.0 → няма тресене
        // intensity = 1.0 → максимално тресене

        double maxOffset = 5 + intensity * 10; // усилва се с опиването

        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(50),
                        new KeyValue(bot.translateXProperty(), (Math.random() - 0.5) * maxOffset),
                        new KeyValue(bot.translateYProperty(), (Math.random() - 0.5) * maxOffset)
                ),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(bot.translateXProperty(), (Math.random() - 0.5) * maxOffset),
                        new KeyValue(bot.translateYProperty(), (Math.random() - 0.5) * maxOffset)
                )
        );

        shake.setCycleCount(Animation.INDEFINITE);
        shake.setAutoReverse(true);
        shake.play();
    }



    private void moveBotToCard(int botId, ImageView bot, Runnable onArrive) {

        double targetX = 0;
        double targetY = 0;

        switch (botId) {
            case 1 -> targetY = 150;   // отгоре → надолу
            case 2 -> targetX = 150;   // отляво → надясно
            case 3 -> targetX = -150;  // отдясно → наляво
            case 4 -> targetY = -150;  // отдолу → нагоре
        }

        // колко силно да залита според пиянството
        double drunkLevel = drunkLevels.get(botId);
        double maxOffset = 10 + drunkLevel * 20;

        // залитането наляво-надясно
        Timeline wobble = new Timeline(
                new KeyFrame(Duration.millis(100), new KeyValue(bot.translateXProperty(), -maxOffset)),
                new KeyFrame(Duration.millis(200), new KeyValue(bot.translateXProperty(), maxOffset))
        );
        wobble.setCycleCount(Animation.INDEFINITE);
        wobble.setAutoReverse(true);
        wobble.play();

        // движение към картата
        TranslateTransition go = new TranslateTransition(Duration.millis(2000), bot);
        go.setToX(targetX);
        go.setToY(targetY);

        go.setOnFinished(e -> {
            wobble.stop(); // спира залитането при пристигане
            onArrive.run();
        });

        go.play();
    }


    private void moveBotBack(ImageView bot) {
        TranslateTransition back = new TranslateTransition(Duration.millis(2000), bot);
        back.setToX(0);
        back.setToY(0);
        back.play();
    }

    private void updateDrunkBarColor(ProgressBar bar, double value) {

        String color;

        if (value < 0.34) {
            color = "green";
        } else if (value < 0.67) {
            color = "orange";
        } else {
            color = "red";
        }

        bar.setStyle("-fx-accent: " + color + ";");
    }


    private void flipCardWithBack() {
        Card c = deckService.drawCard();
        if (c == null) {
            ruleLabel.setText("Няма повече карти!");
            return;
        }

        Image frontImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cards/" + c.getImagePath())));
        Image backImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cards/0.png")));

        ruleLabel.setText(c.getRule());

        int side = random.nextInt(4);
        double startX = 0, startY = 0;
        switch (side) {
            case 0 -> startX = -400;
            case 1 -> startX = 400;
            case 2 -> startY = -400;
            case 3 -> startY = 400;
        }

        cardImage.setTranslateX(startX);
        cardImage.setTranslateY(startY);
        cardImage.setRotationAxis(Rotate.Y_AXIS);
        cardImage.setRotate(0);
        cardImage.setImage(backImg);

        TranslateTransition move = new TranslateTransition(Duration.millis(500), cardImage);
        move.setToX(0);
        move.setToY(0);

        RotateTransition flip1 = new RotateTransition(Duration.millis(250), cardImage);
        flip1.setFromAngle(0);
        flip1.setToAngle(90);
        flip1.setOnFinished(e -> cardImage.setImage(frontImg));

        RotateTransition flip2 = new RotateTransition(Duration.millis(250), cardImage);
        flip2.setFromAngle(90);
        flip2.setToAngle(0);

        TranslateTransition bounce = new TranslateTransition(Duration.millis(150), cardImage);
        bounce.setByY(-20);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);

        SequentialTransition seq = new SequentialTransition(move, flip1, flip2, bounce);
        drawSound.play();
        seq.play();
    }


    public void startPlayerTurn() {
        increaseDrunk(1, 0.1);

        System.out.println("Твой ход!");
        drawButton.setDisable(false);
    }
    public void increaseDrunk(int id, double amount) {
        double lvl = drunkLevels.get(id);
        lvl = Math.min(1.0, lvl + amount);

        drunkLevels.put(id, lvl);

        ProgressBar bar = drunkBars.get(id);

        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(bar.progressProperty(), lvl, Interpolator.EASE_BOTH)
                )
        );
        t.play();

        updateDrunkBarColor(bar, lvl);

        // добавяме пиянското треперене
        ImageView bot = switch (id) {
            case 1 -> botTop;
            case 2 -> botLeft;
            case 3 -> botRight;
            case 4 -> botBottom;
            default -> botBottom;
        };

        shakeBot(bot, lvl);
    }
    private void wobbleBot(ImageView bot, double intensity) {
        // intensity: 0.0 = не залита, 1.0 = много силно
        double maxOffset = 10 + intensity * 20; // колко далече се клати наляво-дясно

        Timeline wobble = new Timeline(
                new KeyFrame(Duration.millis(100),
                        new KeyValue(bot.translateXProperty(), -maxOffset)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(bot.translateXProperty(), maxOffset)
                )
        );
        wobble.setCycleCount(Animation.INDEFINITE);
        wobble.setAutoReverse(true);
        wobble.play();
    }



    public void startBotTurn(int botId) {
        increaseDrunk(botId, 0.1);
        System.out.println("Ход на Бот " + botId);

        ImageView bot = switch (botId) {
            case 1 -> botTop;
            case 2 -> botLeft;
            case 3 -> botRight;
            case 4 -> botBottom;
            default -> botBottom;
        };

        double drunkLevel = drunkLevels.get(botId);
        wobbleBot(bot, drunkLevel);

        playAnimation(bot, "/bots/player" + botId, "walk", 2, 200);


        moveBotToCard(botId, bot, () -> {

            flipCardWithBack();

            moveBotBack(bot);

            new Timeline(new KeyFrame(Duration.seconds(0.8), e -> {
                playAnimation(bot, "/bots/player" + botId, "idle", 2, 500);
                turnManager.nextTurn();
            })).play();
        });
    }
}
