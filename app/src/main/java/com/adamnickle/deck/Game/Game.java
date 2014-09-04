package com.adamnickle.deck.Game;


import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionListener;
import com.adamnickle.deck.spi.GameUiInterface;
import com.adamnickle.deck.spi.GameUiListener;

public abstract class Game implements GameConnectionListener, GameUiListener
{
    protected GameConnectionInterface mGameConnection;
    protected GameUiInterface mGameUI;

    public Game( GameConnectionInterface gameConnectionInterface )
    {
        mGameConnection = gameConnectionInterface;
    }

    public void setGameUiInterface( GameUiInterface gameUiInterface )
    {
        mGameUI = gameUiInterface;
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public abstract void onPlayerConnect( int deviceID, String deviceName );

    @Override
    public abstract void onPlayerDisconnect( int deviceID );

    @Override
    public void onNotification( String notification )
    {
        if( mGameUI != null )
        {
            mGameUI.displayNotification( notification );
        }
    }

    @Override
    public void onConnectionStateChange( int newState )
    {
        switch( newState )
        {
            case ConnectionInterface.STATE_NONE:
                break;

            case ConnectionInterface.STATE_LISTENING:
            case ConnectionInterface.STATE_CONNECTED_LISTENING:
                if( mGameUI != null )
                {
                    mGameUI.displayNotification( "Waiting for more players..." );
                }
                break;

            case ConnectionInterface.STATE_CONNECTING:
                if( mGameUI != null )
                {
                    mGameUI.displayNotification( "Connecting..." );
                }
                break;

            case ConnectionInterface.STATE_CONNECTED:
                if( mGameUI != null )
                {
                    mGameUI.displayNotification( "Connected" );
                }
                break;
        }
    }

    @Override
    public abstract void onCardReceive( int senderID, Card card );

    @Override
    public abstract void onCardSendRequested( int requesterID );

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public abstract boolean onAttemptSendCard( Card card );
}
