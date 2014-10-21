package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;

import java.io.File;


public class ClientGameConnection extends GameConnection
{
    private String mActualServerAddress;

    public ClientGameConnection( ConnectionFragment connectionFragment )
    {
        super( connectionFragment );
    }

    /**
     * *****************************************************************
     * ConnectionListener Methods
     * *****************************************************************
     */
    @Override
    public synchronized void onMessageHandle( GameConnectionListener listener, String originalSenderID, String receiverID, GameMessage message )
    {
        if( originalSenderID.equals( MOCK_SERVER_ADDRESS ) )
        {
            switch( message.getMessageType() )
            {
                case MESSAGE_SET_NAME:
                    final String name = message.getPlayerName();
                    for( GameConnectionListener listener2 : mListeners )
                    {
                        listener2.onServerConnect( MOCK_SERVER_ADDRESS, name );
                    }
                    return;
            }
        }

        super.onMessageHandle( listener, originalSenderID, receiverID, message );
    }

    @Override
    public synchronized void onDeviceConnect( String deviceID, String deviceName )
    {
        mActualServerAddress = deviceID;
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
            mConnectionFragment.findServer();
        }
    }

    @Override
    public boolean saveGame( Context context, String saveName )
    {
        throw new UnsupportedOperationException( "Clients cannot save games." );
    }

    @Override
    public boolean openGameSave( Context context, File gameSave )
    {
        throw new UnsupportedOperationException( "Clients cannot open game saves." );
    }

    @Override
    public void sendMessageToDevice( GameMessage message, String senderID, String receiverID )
    {
        final byte[] data = GameMessage.serializeMessage( message );
        mConnectionFragment.sendDataToDevice( mActualServerAddress, data );
    }
}
