package com.adamnickle.deck.Game;

import java.util.ArrayList;


public class Player
{
    private final String mName;
    private final String mDeviceAddress;
    private final ArrayList<Card> mHand;

    public Player( String deviceAddress, String name )
    {
        mName = name;
        mDeviceAddress = deviceAddress;
        mHand = new ArrayList< Card >();
    }

    public String getAddress()
    {
        return mDeviceAddress;
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

    public Card[] getAllCards()
    {
        return (Card[]) mHand.toArray();
    }
}
