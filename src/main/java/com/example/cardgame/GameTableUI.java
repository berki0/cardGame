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

import java.util.Random;

public class GameTableUI {

    private final DeckService deckService = new DeckService();
    private final AudioClip drawSound = new AudioClip(getClass().getResource("/sounds/draw.mp3").toExternalForm());

    private final BorderPane root = new BorderPane();
    private final ImageView cardImage = new ImageView();
    private final Label ruleLabel = new Label();
    private final Random random = new Random();

    // Ботове
    ImageView botTop = new ImageView(new Image(getClass().getResourceAsStream("/bots/player1.png")));
    ImageView botLeft = new ImageView(new Image(getClass().getResourceAsStream("/bots/player2.png")));
    ImageView botRight = new ImageView(new Image(getClass().getResourceAsStream("/bots/player3.png")));
    ImageView botBottom = new ImageView(new Image(getClass().getResourceAsStream("/bots/player4.png")));

    public GameTableUI() {
        // Настройка на картата
        cardImage.setFitWidth(260);
        cardImage.setFitHeight(360);
        cardImage.setPreserveRatio(true);
        cardImage.setRotationAxis(Rotate.Y_AXIS);

        ruleLabel.setFont(new Font(18));
        ruleLabel.setWrapText(true);
        ruleLabel.setAlignment(Pos.CENTER);

        // Стайлинг на ботовете
        styleBot(botTop);
        styleBot(botLeft);
        styleBot(botRight);
        styleBot(botBottom);

        // Централна зона: карта + правило
        VBox centerBox = new VBox(10, cardImage, ruleLabel);
        centerBox.setAlignment(Pos.CENTER);
        root.setCenter(centerBox);

        // Бутон за теглене
        Button drawButton = new Button("Следваща карта");
        drawButton.setFont(new Font(20));
        drawButton.setOnAction(e -> flipCardWithBack());

        // Горна зона: botTop
        HBox topBox = new HBox(botTop);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        // Лява зона: botLeft
        VBox leftBox = new VBox(botLeft);
        leftBox.setAlignment(Pos.CENTER);
        root.setLeft(leftBox);

        // Дясна зона: botRight
        VBox rightBox = new VBox(botRight);
        rightBox.setAlignment(Pos.CENTER);
        root.setRight(rightBox);

        // Долна зона: botBottom + бутон
        HBox bottomBox = new HBox(10, botBottom, drawButton);
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);

        // Първо показване на карта
        flipCard();
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

        // Стартова позиция извън екрана
        cardImage.setTranslateX(-300);
        cardImage.setTranslateY(-300);
        cardImage.setRotate(0);
        cardImage.setRotationAxis(Rotate.Y_AXIS);

        // Долита към центъра
        TranslateTransition move = new TranslateTransition(Duration.millis(400), cardImage);
        move.setToX(0);
        move.setToY(0);

        // Flip
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
        switch(side) {
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
