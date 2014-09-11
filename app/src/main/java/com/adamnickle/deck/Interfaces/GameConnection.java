package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Game.Player;

import java.util.Arrays;

public abstract class GameConnection implements ConnectionListener, GameConnectionInterface
{
    public static final String MOCK_SERVER_ADDRESS = "mock_server_address";
    public static final String MOCK_SERVER_NAME = "SERVER";

    protected ConnectionInterfaceFragment mConnection;
    protected GameConnectionListener mListener;

    public GameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        mConnection = connection;
        mListener = listener;
    }

    public boolean isServer()
    {
        return mConnection.getConnectionType() == ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER;
    }

    public boolean isGameStarted()
    {
        return mConnection.getState() != ConnectionInterfaceFragment.STATE_NONE;
    }

    /*
    public void setConnectionInterface( ConnectionInterfaceFragment connectionInterfaceFragment )
    {
        mConnection = connectionInterfaceFragment;
    }
    */

    public String getLocalPlayerID()
    {
        return mConnection.getLocalDeviceID();
    }

    public String getDefaultLocalPlayerName()
    {
        return mConnection.getDefaultLocalDeviceName();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageReceive( String senderID, int bytes, byte[] allData )
    {
        final byte[] data = Arrays.copyOf( allData, bytes );
        final GameMessage message = GameMessage.deserializeMessage( data );
        final String originalSenderID = message.getOriginalSenderID();
        final String receiverID = message.getReceiverID();
        switch( message.getMessageType() )
        {
            case MESSAGE_NEW_PLAYER:
                final Player player = new Player( originalSenderID, message.getPlayerName() );
                mListener.onPlayerConnect( player );
                break;

            case MESSAGE_CARD:
                final Card card = message.getCard();
                mListener.onCardReceive( originalSenderID, receiverID, card );
                break;

            case MESSAGE_CARD_REQUEST:
                mListener.onCardRequested( originalSenderID, receiverID ); //TODO Add something about valid cards
                break;

            case MESSAGE_CLEAR_HAND:
                mListener.onClearPlayerHand( originalSenderID, receiverID );
                break;
        }
    }

    @Override
    public void onNotification( String notification )
    {
        mListener.onNotification( notification );
    }

    @Override
    public void onConnectionStateChange( int newState )
    {
        mListener.onConnectionStateChange( newState );
    }

    @Override
    public void onConnectionLost( String deviceID )
    {
        mListener.onPlayerDisconnect( deviceID );
    }

    @Override
    public void onConnectionFailed()
    {
        mListener.onNotification( "Could not connect." );
    }

    /*******************************************************************
     * GameConnection Required Methods
     *******************************************************************/
    public abstract void startGame();
    public abstract void requestCard( String requesterID, String requesteeID );
    public abstract void sendCard( String senderID, String receiverID, Card card );
    public abstract void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID );
}
