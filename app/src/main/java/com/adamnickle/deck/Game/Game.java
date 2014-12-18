package com.adamnickle.deck.Game;


import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable
{
    public static final int MAX_DRAW_PILES = 4;
    public static final int MAX_DISCARD_PILES = 4;

    public int DrawPiles;
    public int DiscardPiles;

    public Game()
    {
        DrawPiles = 0;
        DiscardPiles = 0;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel destination, int flags )
    {
        destination.writeInt( this.DrawPiles );
        destination.writeInt( this.DiscardPiles );
    }

    public static final Parcelable.Creator<Game> CREATOR = new Creator< Game >()
    {
        @Override
        public Game createFromParcel( Parcel source )
        {
            return new Game( source );
        }

        @Override
        public Game[] newArray( int size )
        {
            return new Game[ size ];
        }
    };

    private Game( Parcel in )
    {
        this.DrawPiles = in.readInt();
        this.DiscardPiles = in.readInt();
    }
}
