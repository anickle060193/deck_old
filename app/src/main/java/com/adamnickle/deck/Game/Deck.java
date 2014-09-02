package com.adamnickle.deck.Game;


import java.util.Random;

public final class Deck
{
    private Deck() { }

    public static final Random RANDOM = new Random();
    
    public static final int CARD_COUNT = 52;
    public static final int SUITS = 4;
    public static final int RANKS = 13;

    public static final int SPADES = 0;
    public static final int HEARTS = 1;
    public static final int CLUBS = 2;
    public static final int DIAMONDS = 3;

    public static final String[] SUIT_STRINGS = { "Spades", "Hearts", "Clubs", "Diamonds", };
    public static final String[] RANK_STRINGS = { "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace", };
}
