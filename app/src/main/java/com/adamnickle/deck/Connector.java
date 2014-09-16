package com.adamnickle.deck;


public class Connector
{
    protected String mID;
    protected String mName;

    public Connector( String ID, String name )
    {
        mID = ID;
        mName = name;
    }

    public String getID()
    {
        return mID;
    }

    public String getName()
    {
        return mName;
    }
}
