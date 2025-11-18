package com.example.cardgame;

public class Main {
    public static void main(String[] args) {
        DeckService deck = new DeckService();
        Card c = deck.drawCard();
        System.out.println(c);
    }
}
