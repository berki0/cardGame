package com.example.cardgame;

public class TurnManager {
    private int currentPlayer = 0; // 0 = ти, 1–3 = ботове
    private final GameTableUI ui;

    public TurnManager(GameTableUI ui) {
        this.ui = ui;
    }

    public void start() {
        nextTurn();
    }

    public void nextTurn() {
        currentPlayer = (currentPlayer + 1) % 4;

        switch (currentPlayer) {
            case 0 -> ui.startPlayerTurn(); // Ти
            case 1 -> ui.startBotTurn(1);
            case 2 -> ui.startBotTurn(2);
            case 3 -> ui.startBotTurn(3);
        }
    }
}
