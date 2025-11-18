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
import java.util.Random;

public class GameTableUI {

    private final DeckService deckService = new DeckService();
    private final AudioClip drawSound = new AudioClip(getClass().getResource("/sounds/draw.mp3").toExternalForm());

    private final BorderPane root = new BorderPane();
    private final ImageView cardImage = new ImageView();
    private final Label ruleLabel = new Label();
    private final Random random = new Random();

    // Ботове
    private final ImageView botTop = new ImageView();
    private final ImageView botLeft = new ImageView();
    private final ImageView botRight = new ImageView();
    private final ImageView botBottom = new ImageView();

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
        // Стайлинг на ботовете
        styleBot(botTop);
        styleBot(botLeft);
        styleBot(botRight);
        styleBot(botBottom);

        HBox topBox = new HBox(botTop);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);


        VBox leftBox = new VBox(botLeft);
        leftBox.setAlignment(Pos.CENTER_LEFT); // Подравняване на съдържанието в средата
        root.setLeft(leftBox);


        VBox rightBox = new VBox(botRight);
        rightBox.setAlignment(Pos.CENTER_RIGHT); // Подравняване на съдържанието в средата
        root.setRight(rightBox);

        // Стартираме idle анимации
        playIdle(botTop, "/bots/player1");
        playIdle(botLeft, "/bots/player2");
        playIdle(botRight, "/bots/player3");
        playIdle(botBottom, "/bots/player4");
    }

    private void setupDrawButton() {
        Button drawButton = new Button("Следваща карта");
        drawButton.setFont(new Font(20));
        drawButton.setOnAction(e -> flipCardWithBack());

        HBox bottomBox = new HBox(10, botBottom, drawButton);
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);
    }

    private void playIdle(ImageView bot, String playerPath) {
        int idleIndex = 0; // за сега използваме само idle0
        int frameCount = 2; // промени според броя на рамките
        Image[] frames = new Image[frameCount];

        for (int i = 0; i < frameCount; i++) {
            String path = playerPath + "/idle" + idleIndex + "/" + i + ".png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                frames[i] = new Image(is);
            } else {
                System.out.println("Не може да се намери: " + path);
            }
        }

        bot.setFitWidth(32);
        bot.setFitHeight(32);

        Timeline timeline = new Timeline();
        Duration frameDuration = Duration.millis(500); // Време за показване на една рамка

        for (int i = 0; i < frames.length; i++) {
            final int idx = i;
            // Времето на ключовия кадър трябва да е общата продължителност до този момент
            Duration time = frameDuration.multiply(i);

            timeline.getKeyFrames().add(
                    new KeyFrame(time, e -> bot.setImage(frames[idx]))
            );
        }

        timeline.getKeyFrames().add(
                new KeyFrame(frameDuration.multiply(frameCount), e -> {
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void styleBot(ImageView bot) {
        bot.setFitWidth(32);
        bot.setFitHeight(32);
        bot.setPreserveRatio(true);

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

        Image img = new Image(getClass().getResourceAsStream("/cards/" + c.getImagePath()));
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

        Image frontImg = new Image(getClass().getResourceAsStream("/cards/" + c.getImagePath()));
        Image backImg = new Image(getClass().getResourceAsStream("/cards/0.png"));

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
