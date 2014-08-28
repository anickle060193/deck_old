package com.adamnickle.deck.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Adam on 8/19/2014.
 */
public class CardCollection
{
    public static final int SORT_BY_RANK = 1;
    public static final int SORT_BY_SUIT = 2;

    protected ArrayList<Card> mCards;

    public CardCollection()
    {
        mCards = new ArrayList< Card >();
    }

    public CardCollection( ArrayList<Card> cards )
    {
        mCards = new ArrayList< Card >();
        mCards.addAll( cards );
    }

    public int getCardCount()
    {
        return mCards.size();
    }

    public void addCard( Card card )
    {
        mCards.add( card );
    }

    public void removeCard( Card card )
    {
        mCards.remove( card );
    }

    public Card removeCard( int cardNumber )
    {
        Card remove = null;
        for( Card card : mCards )
        {
            if( card.getCardNumber() == cardNumber )
            {
                remove = card;
                break;
            }
        }
        if( remove != null )
        {
            removeCard( remove );
        }
        return remove;
    }

    public void shuffle()
    {
        Random random = new Random();
        Card card;
        int index;
        for( int i = 0; i < getCardCount(); i++ )
        {
            index = random.nextInt( getCardCount() );
            card = mCards.get( i );
            mCards.set( i, mCards.get( index ) );
            mCards.set( index, card );
        }
    }

    public void sort( int sortType )
    {
        Collections.sort( mCards, new Card.CardComparator( sortType ) );
    }

    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        Iterator<Card> cards = mCards.iterator();
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
