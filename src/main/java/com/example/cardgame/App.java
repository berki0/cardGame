package com.example.cardgame;

import javafx.application.Application;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        GameTableUI ui = new GameTableUI();

        Scene scene = new Scene(ui.getRoot(), 800, 800);
        scene.setCamera(new PerspectiveCamera());  // <-- ТОВА Е ЗАДЪЛЖИТЕЛНО

        stage.setTitle("Drinking Card Game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
