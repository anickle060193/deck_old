package com.adamnickle.deck.Game;

import java.util.ArrayList;


public class Player
{
    private String mName;
    private final String mDeviceID;
    private final ArrayList<Card> mHand;

    public Player( String deviceID, String name )
    {
        mName = name;
        mDeviceID = deviceID;
        mHand = new ArrayList< Card >();
    }

    public String getID()
    {
        return mDeviceID;
    }

    public String getName()
    {
        return mName;
    }

    public void setName( String name )
    {
        mName = name;
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

    public Card[] getAllCards()
    {
        return mHand.toArray( new Card[ mHand.size() ] );
    }

    public void clearHand()
    {
        mHand.clear();
    }
}
