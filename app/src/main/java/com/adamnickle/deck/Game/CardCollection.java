package com.adamnickle.deck.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class CardCollection
{
    public enum SortingType
    {
        SORT_BY_RANK,
        SORT_BY_SUIT,
        SORT_BY_CARD_NUMBER,
    }

    private ArrayList< Card > mCards;

    public CardCollection()
    {
        mCards = new ArrayList< Card >( Deck.CARD_COUNT );
        resetCards();
    }

    public void resetCards()
    {
        mCards.clear();
        for( int i = 0; i < Deck.CARD_COUNT; i++ )
        {
            mCards.add( new Card( i ) );
        }
    }

    public Card[] getCards()
    {
        return mCards.toArray( new Card[ mCards.size() ] );
    }

    public int getCardCount()
    {
        return mCards.size();
    }

    public boolean addCard( Card card )
    {
        if( mCards.size() < Deck.CARD_COUNT && !mCards.contains( card ) )
        {
            mCards.add( mCards.size(), card );
            return true;
        }
        return false;
    }

    public Card removeTopCard()
    {
        return removeCard( 0 );
    }

    public boolean removeCard( Card card )
    {
        return mCards.remove( card );
    }

    public Card removeCard( int index )
    {
        return mCards.remove( index );
    }

    public void shuffle()
    {
        Card card;
        int index;
        for( int i = 0; i < getCardCount(); i++ )
        {
            index = Deck.RANDOM.nextInt( getCardCount() );
            card = mCards.get( i );
            mCards.set( i, mCards.get( index ) );
            mCards.set( index, card );
        }
    }

    public void sort( SortingType sortingType )
    {
        Collections.sort( mCards, new Card.CardComparator( sortingType ) );
    }

    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        Iterator< Card > cards = mCards.iterator();
        Card card;
        while( cards.hasNext() )
        {
            card = cards.next();
            ret.append( card.toString() );
            if( cards.hasNext() )
            {
                ret.append( "\n" );
            }
        }
        return ret.toString();
    }
}
