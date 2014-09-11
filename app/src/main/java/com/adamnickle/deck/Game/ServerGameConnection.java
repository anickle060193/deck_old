package com.adamnickle.deck.Game;

import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;

import java.util.ArrayList;
import java.util.Arrays;


public class ServerGameConnection extends GameConnection
{
    private ArrayList<String> mDeviceIDs;

    public ServerGameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        super( connection, listener );

        mDeviceIDs = new ArrayList< String >();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageReceive( String senderID, int bytes, byte[] allData )
    {
        final byte[] data = Arrays.copyOf( allData, bytes );
        final GameMessage message = GameMessage.deserializeMessage( data );
        final String receiverID = message.getReceiverID();

        if( receiverID.equals( MOCK_SERVER_ADDRESS ) || receiverID.equals( getLocalPlayerID() ) )
        {
            // If message is for this local player, handle normally
            super.onMessageReceive( senderID, bytes, allData );
        }
        else
        {
            // Otherwise, pass on to actual receiver device
            mConnection.sendDataToDevice( receiverID, data );
        }
    }

    @Override
    public void onDeviceConnect( String deviceID, String deviceName )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_NEW_PLAYER, MOCK_SERVER_ADDRESS, null );
        for( String ID : mDeviceIDs )
        {
            message.setReceiverID( ID );
            mConnection.sendDataToDevice( ID, GameMessage.serializeMessage( message ) );
        }
        mDeviceIDs.add( deviceID );
    }

    /*******************************************************************
     * GameConnection Methods
     *******************************************************************/
    @Override
    public void startGame()
    {
        if( !isGameStarted() )
        {
            mConnection.startConnection();
        }
    }

    @Override
    public void requestCard( String requesterID, String requesteeID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_REQUEST, requesterID, requesteeID );
        final byte[] data = GameMessage.serializeMessage( message );

        if( requesteeID.equals( MOCK_SERVER_ADDRESS ) )
        {
            this.onMessageReceive( requesterID, data.length, data );
        }
        else
        {
            mConnection.sendDataToDevice( requesteeID, data );
        }
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD, senderID, receiverID );
        message.putCard( card );
        final byte[] data = GameMessage.serializeMessage( message );

        if( receiverID.equals( MOCK_SERVER_ADDRESS ) )
        {
            this.onMessageReceive( senderID, data.length, data );
        }
        else
        {
            mConnection.sendDataToDevice( receiverID, data );
        }
    }

    @Override
    public void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CLEAR_HAND, commandingDeviceID, toBeClearedDeviceID );
        final byte[] data = GameMessage.serializeMessage( message );

        if( toBeClearedDeviceID.equals( MOCK_SERVER_ADDRESS ) )
        {
            //TODO Local player is telling server to clear hand
            this.onMessageReceive( commandingDeviceID, data.length, data );
        }
        else
        {
            mConnection.sendDataToDevice( toBeClearedDeviceID, data );
        }
    }
}
