package com.adamnickle.deck.Game;

import android.util.SparseArray;

import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionInterface;


public class ServerGame extends Game
{
    private final SparseArray<Player> mRemotePlayers;
    private final CardCollection mDeck;

    private final Player mLocalPlayer;
    private int mCanSendCard;

    public ServerGame( GameConnectionInterface gameConnectionInterface )
    {
        super( gameConnectionInterface );
        mRemotePlayers = new SparseArray< Player >();
        mDeck = new CardCollection();
        mLocalPlayer = new Player( ConnectionInterface.LOCAL_DEVICE_ID, "Local Player" );
        mCanSendCard = 0;
    }

    private void requestCardFromPlayer( int ID )
    {
        if( ID == mLocalPlayer.getID() )
        {
            mCanSendCard++;
        }
        else
        {
            mGameConnection.requestCard( ID );
        }
    }

    private void sendCardToPlayer( int ID, Card card )
    {
        if( ID == mLocalPlayer.getID() )
        {
            mLocalPlayer.addCard( card );
        }
        else
        {
            mGameConnection.sendCard( ID, card );
        }
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public void onPlayerConnect( int deviceID, String deviceName )
    {
        mRemotePlayers.put( deviceID, new Player( deviceID, deviceName ) );
    }

    @Override
    public void onPlayerDisconnect( int deviceID )
    {
        Player removePlayer = mRemotePlayers.get( deviceID );
        if( removePlayer != null )
        {
            mGameUI.displayNotification( removePlayer.getName() + " has disconnected." );
        }
    }

    @Override
    public void onCardReceive( int senderID, Card card )
    {
        mDeck.addCard( card );
    }

    @Override
    public void onCardSendRequested( int requesterID )
    {
        // Why is player asking server for card?
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( Card card )
    {
        if( mCanSendCard > 0 )
        {
            this.onCardReceive( mLocalPlayer.getID(), card );
            mCanSendCard--;
            return true;
        }
        else
        {
            return false;
        }
    }
}
