package com.adamnickle.deck;

/**
 * Created by Adam on 8/18/2014.
 */
public class Deck extends CardCollection
{
    public static final int CARD_COUNT = 52;
    public static final int SUITS = 4;
    public static final int RANKS = 13;

    public static final String[] SUIT_STRINGS = { "Spades", "Hearts", "Clubs", "Diamonds", };
    public static final String[] RANK_STRINGS = { "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace", };

    public Card dealCard()
    {
        Card dealt = null;
        if( !mCards.isEmpty() )
        {
            dealt = mCards.get( 0 );
            removeCard( dealt );
        }
        return dealt;
    }
}
