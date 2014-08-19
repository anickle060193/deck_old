package com.adamnickle.deck;

import java.security.InvalidParameterException;

/**
 * Created by Adam on 8/18/2014.
 */
public class Card
{
    private int mSuit;
    private int mRank;

    public Card( int card )
    {
        if( card >= Deck.CARD_COUNT || card < 0 )
        {
            throw new InvalidParameterException( "Card parameter must be a non-negative integer less than " + Integer.toString( Deck.CARD_COUNT ) );
        }

        mSuit = card / Deck.RANKS;
        mRank = card % Deck.RANKS;
    }

    public Card( int suit, int rank )
    {
        if( suit >= Deck.SUITS || suit < 0 )
        {
            throw new InvalidParameterException( "Suit parameter must be a non-negative integer less than " + Integer.toString( Deck.SUITS ) );
        }
        if( rank >= Deck.SUITS || rank < 0 )
        {
            throw new InvalidParameterException( "Rank parameter must be a non-negative integer less than " + Integer.toString( Deck.RANKS ) );
        }

        mSuit = suit;
        mRank = rank;
    }
}
