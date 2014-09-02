package com.adamnickle.deck.Game;


import com.adamnickle.deck.BluetoothConnection;
import com.adamnickle.deck.spi.GameConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionListener;
import com.adamnickle.deck.spi.GameUiInterface;
import com.adamnickle.deck.spi.GameUiListener;

public abstract class Game implements GameConnectionListener, GameUiListener
{
    protected GameConnectionInterface mGameConnection;
    protected GameUiInterface mGameUI;
    /*
    public Game( GameConnectionInterface gameConnectionInterface, GameUiInterface gameUiInterface )
    {
        mGameConnection = gameConnectionInterface;
        mGameUI = gameUiInterface;
    }
    */
    public void setGameConnectionInterface( GameConnectionInterface gameConnectionInterface )
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
        mGameUI.displayNotification( notification );
    }

    @Override
    public void onConnectionStateChange( int newState )
    {
        switch( newState )
        {
            case BluetoothConnection.STATE_NONE:
                break;

            case BluetoothConnection.STATE_LISTENING:
            case BluetoothConnection.STATE_CONNECTED_LISTENING:
                mGameUI.displayNotification( "Waiting for more players..." );
                break;

            case BluetoothConnection.STATE_CONNECTING:
                mGameUI.displayNotification( "Connecting..." );
                break;

            case BluetoothConnection.STATE_CONNECTED:
                mGameUI.displayNotification( "Connected" );
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
