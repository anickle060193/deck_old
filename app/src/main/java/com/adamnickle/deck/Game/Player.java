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

    public boolean hasCard( Card card )
    {
        return mHand.contains( card );
    }

    public void removeCard( Card card )
    {
        mHand.remove( card );
    }

    public void addCard( Card card )
    {
        mHand.add( card );
    }

    public void clearHand()
    {
        mHand.clear();
    }
}
