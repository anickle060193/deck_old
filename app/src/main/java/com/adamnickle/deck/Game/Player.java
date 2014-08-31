package com.adamnickle.deck.Game;

import java.util.ArrayList;
import java.util.Collection;


public class Player
{
    private String mName;
    private ArrayList<Card> mHand;

    public Player( String name )
    {
        mName = name;
        mHand = new ArrayList< Card >();
    }

    public boolean isStillPlaying()
    {
        return getCardCount() > 0;
    }

    public int getCardCount()
    {
        return mHand.size();
    }

    public void addCard( Card card )
    {
        mHand.add( card );
    }

    public void addCards( Collection<Card> cards )
    {
        mHand.addAll( cards );
    }

    public Card playCard()
    {
        Card card = null;
        if( mHand.size() > 0 )
        {
            final int index = Game.RANDOM.nextInt( mHand.size() );
            card = mHand.remove( index );
        }
        return card;
    }
}
