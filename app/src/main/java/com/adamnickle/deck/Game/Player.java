package com.adamnickle.deck.Game;

import java.util.ArrayList;
import java.util.Collection;


public class Player
{
    private final String mName;
    private final int mDeviceID;
    private final ArrayList<Card> mHand;

    public Player( int deviceID, String name )
    {
        mName = name;
        mDeviceID = deviceID;
        mHand = new ArrayList< Card >();
    }

    public int getID()
    {
        return mDeviceID;
    }

    public String getName()
    {
        return mName;
    }

    public boolean hasCards()
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
}
