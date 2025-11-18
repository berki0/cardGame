package com.example.cardgame;

import java.util.*;

public class DeckService {

    private final List<Card> deck = new ArrayList<>();
    private final Map<String, String> rules = new HashMap<>();

    public DeckService() {
        loadRules();
        loadDeck();
        shuffle();
    }

    private void loadRules() {
        rules.put("7",  "Пий 2 глътки.");
        rules.put("8",  "Дай 2 глътки.");
        rules.put("9",  "Кажи дума – следващият трябва да римува.");
        rules.put("10", "Избери някой да пие.");
        rules.put("J",  "Всички пият.");
        rules.put("Q",  "Измисли правило.");
        rules.put("K",  "Разбий кръга!");
        rules.put("A",  "Водопад!");
    }

    private void loadDeck() {
        String[] ranks = {"7","8","9","10","J","Q","K","A"};
        String[] suits = {"hearts","spades","clubs","diamonds"};

        for (String rank : ranks) {
            for (String suit : suits) {
                deck.add(new Card(rank, suit, rules.get(rank)));
            }
        }
    }

    public void shuffle() { Collections.shuffle(deck); }

    public Card drawCard() {
        if (deck.isEmpty()) return null;
        return deck.remove(0);
    }
}
