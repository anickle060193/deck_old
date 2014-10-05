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

    /*******************************************************************
     * GameConnection Methods
     *******************************************************************/
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
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_RECEIVE_CARD, senderID, receiverID );
        message.putCard( card );
        this.sendMessageToDevice( message, senderID, receiverID );
    }
}
