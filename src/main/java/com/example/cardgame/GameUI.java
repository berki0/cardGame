package com.example.cardgame;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class GameUI {

    private final DeckService deckService = new DeckService();

    private final VBox root = new VBox(20);
    private final ImageView cardImage = new ImageView();
    private final Label ruleLabel = new Label();

    public GameUI() {
        root.setAlignment(Pos.CENTER);

        // Настройка размера на картата
        cardImage.setFitWidth(300);
        cardImage.setFitHeight(420);
        cardImage.setPreserveRatio(true);

        ruleLabel.setFont(new Font(20));
        ruleLabel.setWrapText(true);
        ruleLabel.setMaxWidth(400);
        ruleLabel.setAlignment(Pos.CENTER);

        Button drawButton = new Button("Следваща карта");
        drawButton.setFont(new Font(18));

        drawButton.setOnAction(e -> drawCard());

        root.getChildren().addAll(cardImage, ruleLabel, drawButton);

        drawCard();
    }

    public Pane getRoot() {
        return root;
    }

    private void drawCard() {
        Card c = deckService.drawCard();

        Image img = new Image(getClass().getResourceAsStream("/cards/" + c.getImagePath()));

        cardImage.setRotationAxis(Rotate.Y_AXIS);

        RotateTransition flip1 = new RotateTransition(Duration.millis(200), cardImage);
        flip1.setFromAngle(0);
        flip1.setToAngle(90);

        RotateTransition flip2 = new RotateTransition(Duration.millis(200), cardImage);
        flip2.setFromAngle(90);
        flip2.setToAngle(180);

        flip1.setOnFinished(e -> cardImage.setImage(img));

        SequentialTransition seq = new SequentialTransition(flip1, flip2);
        seq.play();
    }
}
