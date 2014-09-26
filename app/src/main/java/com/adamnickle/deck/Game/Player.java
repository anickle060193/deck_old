package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import com.adamnickle.deck.Interfaces.CardHolderListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class Player
{
    protected String mID;
    protected String mName;
    protected final ArrayList< Card > mCards;
    protected CardHolderListener mListener;

    private Player()
    {
        mCards = new ArrayList< Card >();
    }

    public Player( String deviceID, String name )
    {
        this();
        mID = deviceID;
        mName = name;
    }

    public void setCardHolderListener( CardHolderListener cardHolderListener )
    {
        mListener = cardHolderListener;
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

    public int getCardCount()
    {
        return mCards.size();
    }

    public boolean hasCard( Card card )
    {
        return mCards.contains( card );
    }

    public boolean removeCard( Card card )
    {
        if( mListener != null )
        {
            mListener.onCardRemoved( mID, card );
        }
        return mCards.remove( card );
    }

    public boolean removeCards( Card[] cards )
    {
        if( mListener != null )
        {
            mListener.onCardsRemoved( mID, cards );
        }
        return mCards.removeAll( Arrays.asList( cards ) );
    }

    public boolean addCard( Card card )
    {
        if( mListener != null )
        {
            mListener.onCardAdded( mID, card );
        }
        return mCards.add( card );
    }

    public boolean addCards( Card[] cards )
    {
        if( mListener != null )
        {
            mListener.onCardsAdded( mID, cards );
        }
        return mCards.addAll( Arrays.asList( cards ) );
    }

    public void clearCards()
    {
        if( mListener != null )
        {
            mListener.onCardsCleared( mID );
        }
        mCards.clear();
    }

    public Card[] getCards()
    {
        return mCards.toArray( new Card[ mCards.size() ] );
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

        return mID.equals( ( (Player) o ).mID );
    }

    @Override
    public String toString()
    {
        return mName;
    }

    @Override
    public int hashCode()
    {
        return mID.hashCode();
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginObject();
        writer.name( "player_id" ).value( getID() );
        writer.name( "player_name" ).value( getName() );
        writer.name( "cards" ).beginArray();
        for( Card card : mCards )
        {
            card.writeToJson( writer );
        }
        writer.endArray();
        writer.endObject();
    }

    public static Player readFromJson( JsonReader reader ) throws IOException
    {
        Player player = new Player();

        reader.beginObject();
        while( reader.hasNext() )
        {
            String name = reader.nextName();
            if( name.equals( "player_id" ) )
            {
                player.mID = reader.nextString();
            }
            else if( name.equals( "player_name" ) )
            {
                player.mName = reader.nextString();
            }
            else if( name.equals( "cards" ) && reader.peek() != JsonToken.NULL )
            {
                reader.beginArray();
                while( reader.hasNext() )
                {
                    player.addCard( Card.readFromJson( reader ) );
                }
                reader.endArray();
            }
            else
            {
                reader.skipValue();
            }
        }
        reader.endObject();

        return player;
    }
}
