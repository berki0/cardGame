package com.example.cardgame;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private final BorderPane root = new BorderPane();
    private final ImageView cardImage = new ImageView();
    private final Label ruleLabel = new Label();
    private final Random random = new Random();

    private final ImageView botTop = new ImageView();
    private final ImageView botLeft = new ImageView();
    private final ImageView botRight = new ImageView();
    private final ImageView botBottom = new ImageView();

    // държим текущите timeline-и за всеки ImageView, за да можем да ги спираме
    private final Map<ImageView, Timeline> runningAnimations = new HashMap<>();

    public GameTableUI() {
        setupCardArea();
        setupBots();
        setupDrawButton();

        flipCard(); // първа карта
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
        // стил
        styleBot(botTop);
        styleBot(botLeft);
        styleBot(botRight);
        styleBot(botBottom);

        // позициониране в BorderPane (внимавай да не добавяш един и същ ImageView на няколко места)
        HBox topBox = new HBox(botTop);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        VBox leftBox = new VBox(botLeft);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        root.setLeft(leftBox);

        VBox rightBox = new VBox(botRight);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        root.setRight(rightBox);

        // bottom ще бъде заменен от setupDrawButton() — тук ще добавим само botBottom placeholder
        HBox bottomPlaceholder = new HBox(botBottom);
        bottomPlaceholder.setAlignment(Pos.CENTER);
        root.setBottom(bottomPlaceholder);

        // стартираме initial idle за всички ботове
        playAnimation(botTop, "/bots/player1", "idle", 2, 500);
        playAnimation(botLeft, "/bots/player2", "idle", 2, 500);
        playAnimation(botRight, "/bots/player3", "idle", 2, 500);
        playAnimation(botBottom, "/bots/player4", "idle", 2, 500);

        // Пример: след 3s да преминат към walk (поправено да използва правилните ImageView)
        new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            playAnimation(botTop, "/bots/player1", "walk", 2, 200);
        })).play();

        new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            playAnimation(botLeft, "/bots/player2", "walk", 2, 200);
        })).play();

        new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            playAnimation(botRight, "/bots/player3", "walk", 2, 200);
        })).play();

        new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            playAnimation(botBottom, "/bots/player4", "walk", 2, 200);
        })).play();
    }

    private void setupDrawButton() {
        Button drawButton = new Button("Следваща карта");
        drawButton.setFont(new Font(20));
        drawButton.setOnAction(e -> {
            // пример: когато човек тегли карта, всички ботове за секунда преминават в walk, след което се връщат в idle
            playAnimation(botTop, "/bots/player1", "walk", 2, 200);
            playAnimation(botLeft, "/bots/player2", "walk", 2, 200);
            playAnimation(botRight, "/bots/player3", "walk", 2, 200);
            playAnimation(botBottom, "/bots/player4", "walk", 2, 200);

            // след 1s връщаме обратно на idle
            new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                playAnimation(botTop, "/bots/player1", "idle", 2, 500);
                playAnimation(botLeft, "/bots/player2", "idle", 2, 500);
                playAnimation(botRight, "/bots/player3", "idle", 2, 500);
                playAnimation(botBottom, "/bots/player4", "idle", 2, 500);
            })).play();

            // и извикваме действието (пусни картата)
            flipCardWithBack();
        });

        HBox bottomBox = new HBox(10, botBottom, drawButton);
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);
    }

    /**
     * Универсален loader + player за анимации.
     * Спира вече работеща анимация за същия ImageView (ако има),
     * зарежда наличните рамки и стартира нов Timeline.
     */
    private void playAnimation(ImageView bot, String playerPath, String anim, int frameCountHint, int speedMs) {
        // стопни предишната
        Timeline prev = runningAnimations.get(bot);
        if (prev != null) {
            prev.stop();
        }

        // зареждаме рамките — динамично, ако някой рамка липсва я пропускаме
        List<Image> framesList = new ArrayList<>();
        for (int i = 0; i < frameCountHint; i++) {
            String path = playerPath + "/" + anim + "/" + i + ".png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.out.println("Missing resource: " + path);
                continue;
            }
            Image img = new Image(is, 0, 0, true, false); // 0,0 => оригинален размер, preserveRatio=true, smooth=false
            framesList.add(img);
        }

        if (framesList.isEmpty()) {
            System.out.println("No frames loaded for " + playerPath + "/" + anim + " — aborting animation.");
            return;
        }

        // build timeline
        Timeline timeline = new Timeline();
        Duration frameDuration = Duration.millis(Math.max(1, speedMs)); // avoid 0

        for (int i = 0; i < framesList.size(); i++) {
            final Image frame = framesList.get(i);
            Duration at = frameDuration.multiply(i);
            timeline.getKeyFrames().add(new KeyFrame(at, ev -> bot.setImage(frame)));
        }
        // ensure loop — we add a blank KeyFrame at total duration so cycle works cleanly
        timeline.getKeyFrames().add(new KeyFrame(frameDuration.multiply(framesList.size()), ev -> {}));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        runningAnimations.put(bot, timeline);

        // опционално: сетваме fit size (можеш да регулираш)
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

        TranslateTransition move = new TranslateTransition(Duration.millis(400), cardImage);
        move.setToX(0);
        move.setToY(0);

        RotateTransition flip1 = new RotateTransition(Duration.millis(200), cardImage);
        flip1.setFromAngle(0);
        flip1.setToAngle(90);
        flip1.setOnFinished(e -> cardImage.setImage(img));

        RotateTransition flip2 = new RotateTransition(Duration.millis(200), cardImage);
        flip2.setFromAngle(90);
        flip2.setToAngle(0);

        SequentialTransition seq = new SequentialTransition(move, flip1, flip2);
        drawSound.play();
        seq.play();
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
}
