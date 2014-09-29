package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;


public class ClientGameConnection extends GameConnection
{
    private String mActualServerAddress;

    public ClientGameConnection( Connection connection )
    {
        super( connection );
    }

    /**
     * *****************************************************************
     * ConnectionListener Methods
     * *****************************************************************
     */
    @Override
    public synchronized void onDeviceConnect( String deviceID, String deviceName )
    {
        mActualServerAddress = deviceID;
        for( GameConnectionListener listener : mListeners )
        {
            listener.onServerConnect( MOCK_SERVER_ADDRESS, MOCK_SERVER_NAME );
        }
    }

    @Override
    public synchronized void onConnectionLost( String deviceID )
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onServerDisconnect( deviceID );
        }
    }

    /**
     * *****************************************************************
     * GameConnection Methods
     * *****************************************************************
     */
    @Override
    public void startGame()
    {
        if( !isGameStarted() )
        {
            mConnection.findServer();
        }
    }

    @Override
    public boolean saveGame( Context context, String saveName )
    {
        throw new UnsupportedOperationException( "Clients cannot save games." );
    }

    @Override
    public boolean openGameSave( Context context, GameSave gameSave )
    {
        throw new UnsupportedOperationException( "Clients cannot open game saves." );
    }

    @Override
    public void sendMessageToDevice( GameMessage message, String senderID, String receiverID )
    {
        if( receiverID.equals( getLocalPlayerID() ) )
        {
            final GameConnectionListener listener = this.findAppropriateListener( message.getMessageType(), senderID, receiverID );
            this.onMessageHandle( listener, senderID, receiverID, message );
        }
        else
        {
            final byte[] data = GameMessage.serializeMessage( message );
            mConnection.sendDataToDevice( mActualServerAddress, data );
        }
    }

    @Override
    public void requestCard( String requesterID, String requesteeID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_REQUEST, requesterID, requesteeID );
        this.sendMessageToDevice( message, requesterID, requesteeID );
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card, boolean removingFromHand )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD, senderID, receiverID );
        message.putCard( card, removingFromHand );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    @Override
    public void sendCards( String senderID, String receiverID, Card[] cards, boolean removingFromHand )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARDS, senderID, receiverID );
        message.putCards( cards, removingFromHand );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    @Override
    public void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CLEAR_HAND, commandingDeviceID, toBeClearedDeviceID );
        this.sendMessageToDevice( message, commandingDeviceID, toBeClearedDeviceID );
    }

    @Override
    public void setDealer( String setterID, String setteeID, boolean isDealer )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_DEALER, setterID, setteeID );
        message.putIsDealer( isDealer );
        this.sendMessageToDevice( message, setterID, setteeID );
    }

    @Override
    public void sendPlayerName( String senderID, String receiverID, String name )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_PLAYER_NAME, senderID, receiverID );
        message.putName( name );
        this.sendMessageToDevice( message, senderID, receiverID );
    }
}
