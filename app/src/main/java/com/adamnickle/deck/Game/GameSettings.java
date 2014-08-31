package com.adamnickle.deck.Game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Adam on 8/19/2014.
 */
public class GameSettings implements Parcelable
{
    public String GameName;
    public int MinimumPlayers;
    public int MaximumPlayers;
    public boolean TrackPoints;
    public int[] CardPointValues;
    public boolean DealFullDeck;
    public int InitialCardsPerPlayer;

    public boolean DrawPile;
    public boolean DiscardPile;
    public boolean Teams;
    public boolean AcesHigh;
    public int CardRankingType;

    public GameSettings()
    {
        GameName = "New Game";
        MinimumPlayers = 2;
        MaximumPlayers = 4;
        TrackPoints = false;
        CardPointValues = null;
        DealFullDeck = true;
        InitialCardsPerPlayer = 0;
    }

    public GameSettings( Parcel in )
    {
        GameName = in.readString();
        MinimumPlayers = in.readInt();
        MaximumPlayers = in.readInt();
        TrackPoints = in.readInt() == 1;
        in.readIntArray( CardPointValues );
        DealFullDeck = in.readInt() == 1;
        InitialCardsPerPlayer = in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel parcel, int flags )
    {
        parcel.writeString( GameName );
        parcel.writeInt( MinimumPlayers );
        parcel.writeInt( MaximumPlayers );
        parcel.writeInt( TrackPoints ? 1 : 0 );
        parcel.writeIntArray( CardPointValues );
        parcel.writeInt( DealFullDeck ? 1 : 0 );
        parcel.writeInt( InitialCardsPerPlayer );
    }
}
