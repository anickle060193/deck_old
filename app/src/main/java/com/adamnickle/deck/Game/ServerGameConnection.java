package com.adamnickle.deck.Game;

import com.adamnickle.deck.Connector;
import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;

import java.util.Arrays;
import java.util.HashMap;

public class ServerGameConnection extends GameConnection
{
    private HashMap< String, Connector > mConnectors;

    public ServerGameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        super( connection, listener );

        mConnectors = new HashMap< String, Connector >();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public synchronized void onMessageReceive( String senderID, int bytes, byte[] allData )
    {
        final byte[] data = Arrays.copyOf( allData, bytes );
        final GameMessage message = GameMessage.deserializeMessage( data );
        final String originalSenderID = message.getOriginalSenderID();
        final String receiverID = message.getReceiverID();

        if( receiverID.equals( MOCK_SERVER_ADDRESS ) )
        {
            switch( message.getMessageType() )
            {
                case MESSAGE_SET_PLAYER_NAME:
                    final String newName = message.getPlayerName();
                    for( Connector connector : mConnectors.values() )
                    {
                        if( connector.getID().equals( originalSenderID ) )
                        {
                            connector.setName( newName );
                        }
                        else
                        {
                            this.sendPlayerName( originalSenderID, connector.getID(), newName );
                        }
                    }
                    break;

                case MESSAGE_CARD:
                    //TODO Server received card from player
                    break;
            }
        }
        else if( receiverID.equals( getLocalPlayerID() ) )
        {
            super.onMessageReceive( senderID, bytes, allData );
        }
        else
        {
            // Otherwise, pass on to actual receiver device
            mConnection.sendDataToDevice( receiverID, data );
        }
    }

    @Override
    public synchronized void onDeviceConnect( String deviceID, String deviceName )
    {
        if( mConnectors.size() > 0 )
        {
            // Send the new player information about already connected players
            final GameMessage currentPlayersMessage = new GameMessage( GameMessage.MessageType.MESSAGE_CURRENT_PLAYERS, MOCK_SERVER_ADDRESS, deviceID );
            currentPlayersMessage.putCurrentPlayers( mConnectors.values().toArray( new Connector[ mConnectors.size() ] ) );
            final byte[] currentPlayersData = GameMessage.serializeMessage( currentPlayersMessage );
            mConnection.sendDataToDevice( deviceID, currentPlayersData );
        }

        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_NEW_PLAYER, deviceID, null );
        message.putName( deviceName );

        // Send new player to all connected remote players
        for( Connector connector : mConnectors.values() )
        {
            if( !connector.getID().equals( getLocalPlayerID() ) )
            {
                message.setReceiverID( connector.getID() );
                mConnection.sendDataToDevice( connector.getID(), GameMessage.serializeMessage( message ) );
            }
        }

        // Send new player to local player
        message.setReceiverID( getLocalPlayerID() );
        final byte data[] = GameMessage.serializeMessage( message );
        this.onMessageReceive( deviceID, data.length, data );

        mConnectors.put( deviceID, new Connector( deviceID, deviceName ) );
    }

    @Override
    public synchronized void onConnectionLost( String deviceID )
    {
        mConnectors.remove( deviceID );
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_PLAYER_LEFT, deviceID, null );

        // Send player left to all connected remote players
        for( Connector connector : mConnectors.values() )
        {
            if( !connector.getID().equals( getLocalPlayerID() ) )
            {
                message.setReceiverID( connector.getID() );
                mConnection.sendDataToDevice( connector.getID(), GameMessage.serializeMessage( message ) );
            }
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
            this.onDeviceConnect( getLocalPlayerID(), getDefaultLocalPlayerName() );
            mListener.onServerConnect( MOCK_SERVER_ADDRESS, MOCK_SERVER_NAME );
        }
    }

    @Override
    public void requestCard( String requesterID, String requesteeID )
    {
        if( requesteeID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardRequested( requesterID, requesteeID );
        }
        else
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
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card )
    {
        if( receiverID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardReceive( senderID, receiverID, card );
        }
        else
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
    }

    @Override
    public void sendCards( String senderID, String receiverID, Card[] cards )
    {
        if( receiverID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardsReceive( senderID, receiverID, cards );
        }
        else
        {
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARDS, senderID, receiverID );
            message.putCards( cards );
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
    }

    @Override
    public void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID )
    {
        if( toBeClearedDeviceID.equals( getLocalPlayerID() ) )
        {
            mListener.onClearPlayerHand( commandingDeviceID, toBeClearedDeviceID );
        }
        else
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

    @Override
    public void setDealer( String setterID, String setteeID, boolean isDealer )
    {
        if( setteeID.equals( getLocalPlayerID() ) )
        {
            mListener.onSetDealer( setterID, setteeID, isDealer );
        }
        else
        {
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_DEALER, setterID, setteeID );
            message.putIsDealer( isDealer );
            final byte[] data = GameMessage.serializeMessage( message );

            if( setteeID.equals( MOCK_SERVER_ADDRESS ) )
            {
                //TODO Local player set server as dealer
            }
            else
            {
                mConnection.sendDataToDevice( setteeID, data );
            }
        }
    }

    @Override
    public void sendPlayerName( String senderID, String receiverID, String name )
    {
        if( receiverID.equals( getLocalPlayerID() ) )
        {
            mListener.onPlayerNameReceive( senderID, name );
        }
        else
        {
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_PLAYER_NAME, senderID, receiverID );
            message.putName( name );
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
    }
}
