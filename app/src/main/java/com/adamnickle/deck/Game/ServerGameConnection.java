package com.adamnickle.deck.Game;

import com.adamnickle.deck.Connector;
import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;

import java.util.ArrayList;
import java.util.Arrays;


public class ServerGameConnection extends GameConnection
{
    private ArrayList< Connector > mConnectors;

    public ServerGameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        super( connection, listener );

        mConnectors = new ArrayList< Connector >();
        mConnectors.add( new Connector( getLocalPlayerID(), getDefaultLocalPlayerName() ) );
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
        // Send the new player information about already connected players
        final GameMessage currentPlayersMessage = new GameMessage( GameMessage.MessageType.MESSAGE_CURRENT_PLAYERS, MOCK_SERVER_ADDRESS, deviceID );
        currentPlayersMessage.putCurrentPlayers( mConnectors.toArray( new Connector[ mConnectors.size() ] ) );
        final byte[] currentPlayersData = GameMessage.serializeMessage( currentPlayersMessage );
        mConnection.sendDataToDevice( deviceID, currentPlayersData );

        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_NEW_PLAYER, deviceID, null );
        message.putName( deviceName );

        // Send new player to all connected remote players
        for( Connector connector : mConnectors )
        {
            message.setReceiverID( connector.getID() );
            mConnection.sendDataToDevice( connector.getID(), GameMessage.serializeMessage( message ) );
        }

        // Send new player to local player
        message.setReceiverID( getLocalPlayerID() );
        final byte data[] = GameMessage.serializeMessage( message );
        this.onMessageReceive( deviceID, data.length, data );

        mConnectors.add( new Connector( deviceID, deviceName ) );
    }

    @Override
    public void onConnectionLost( String deviceID )
    {
        mConnectors.remove( deviceID );
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_PLAYER_LEFT, deviceID, null );

        // Send player left to all connected remote players
        for( Connector connector : mConnectors )
        {
            message.setReceiverID( connector.getID() );
            mConnection.sendDataToDevice( connector.getID(), GameMessage.serializeMessage( message ) );
        }

        // Send player left to local player
        message.setReceiverID( getLocalPlayerID() );
        final byte data[] = GameMessage.serializeMessage( message );
        this.onMessageReceive( deviceID, data.length, data );
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
        else if( requesteeID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardRequested( requesterID, requesteeID );
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
        else if( receiverID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardReceive( senderID, receiverID, card );
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
        else if( toBeClearedDeviceID.equals( getLocalPlayerID() ) )
        {
            mListener.onClearPlayerHand( commandingDeviceID, toBeClearedDeviceID );
        }
        else
        {
            mConnection.sendDataToDevice( toBeClearedDeviceID, data );
        }
    }

    @Override
    public void setDealer( String setterID, String setteeID, boolean isDealer )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_DEALER, setterID, setteeID );
        message.putIsDealer( isDealer );
        final byte[] data = GameMessage.serializeMessage( message );

        if( setteeID.equals( MOCK_SERVER_ADDRESS ) )
        {
            //TODO Local player set server as dealer
        }
        else if( setteeID.equals( getLocalPlayerID() ) )
        {
            mListener.onSetDealer( setterID, setteeID, isDealer );
        }
        else
        {
            mConnection.sendDataToDevice( setteeID, data );
        }
    }
}
