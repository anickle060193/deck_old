package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;


public class ClientGameConnection extends GameConnection
{
    private String mActualServerAddress;

    public ClientGameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        super( connection, listener );
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
        mListener.onServerConnect( MOCK_SERVER_ADDRESS, MOCK_SERVER_NAME );
    }

    @Override
    public synchronized void onConnectionLost( String deviceID )
    {
        mListener.onServerDisconnect( deviceID );
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
    public void requestCard( String requesterID, String requesteeID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_REQUEST, requesterID, requesteeID );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card, boolean removingFromHand )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD, senderID, receiverID );
        message.putCard( card, removingFromHand );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void sendCards( String senderID, String receiverID, Card[] cards, boolean removingFromHand )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARDS, senderID, receiverID );
        message.putCards( cards, removingFromHand );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CLEAR_HAND, commandingDeviceID, toBeClearedDeviceID );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void setDealer( String setterID, String setteeID, boolean isDealer )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_DEALER, setterID, setteeID );
        message.putIsDealer( isDealer );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void sendPlayerName( String senderID, String receiverID, String name )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_PLAYER_NAME, senderID, receiverID );
        message.putName( name );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }
}
