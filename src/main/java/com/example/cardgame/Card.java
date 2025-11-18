package com.example.cardgame;

public class Card {
    private final String rank;
    private final String suit;
    private final String rule;
    private final String imagePath;

    public Card(String rank, String suit, String rule) {
        this.rank = rank;
        this.suit = suit;
        this.rule = rule;
        this.imagePath = rank + "_" + suit + ".png";
    }

    public String getRank() { return rank; }
    public String getSuit() { return suit; }
    public String getRule() { return rule; }
    public String getImagePath() { return imagePath; }

    @Override
    public String toString() {
        return rank + " of " + suit + " -> " + rule;
    }
}
