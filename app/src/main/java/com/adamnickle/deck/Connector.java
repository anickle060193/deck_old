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

    public void setName( String name )
    {
        mName = name;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Connector connector = (Connector) o;

        if( mID != null ? !mID.equals( connector.mID ) : connector.mID != null )
        {
            return false;
        }

        if( mName != null ? !mName.equals( connector.mName ) : connector.mName != null )
        {
            return false;
        }

        return true;
    }
}
