package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;

import java.util.HashMap;

public class ServerGameConnection extends GameConnection
{
    private HashMap< String, CardHolder > mPlayers;
    private HashMap< String, CardHolder > mLeftPlayers;

    public ServerGameConnection( Connection connection )
    {
        super( connection );

        mPlayers = new HashMap< String, CardHolder >();
        mLeftPlayers = new HashMap< String, CardHolder >();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageHandle( GameConnectionListener listener, String originalSenderID, String receiverID, GameMessage message )
    {
        if( receiverID.equals( MOCK_SERVER_ADDRESS ) )
        {
            switch( message.getMessageType() )
            {
                case MESSAGE_SET_PLAYER_NAME:
                    final String newName = message.getPlayerName();
                    for( CardHolder player : mPlayers.values() )
                    {
                        if( player.getID().equals( originalSenderID ) )
                        {
                            player.setName( newName );
                        }
                        else
                        {
                            this.sendPlayerName( originalSenderID, player.getID(), newName );
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
            super.onMessageHandle( listener, originalSenderID, receiverID, message );
        }
        else
        {
            // Otherwise, pass on to actual receiver device
            mConnection.sendDataToDevice( receiverID, GameMessage.serializeMessage( message ) );

            switch( message.getMessageType() )
            {
                case MESSAGE_CARD:
                    mPlayers.get( receiverID ).addCard( message.getCard() );
                    if( message.getFromPlayerHand() )
                    {
                        mPlayers.get( originalSenderID ).removeCard( message.getCard() );
                    }
                    break;

                case MESSAGE_CARDS:
                    mPlayers.get( receiverID ).addCards( message.getCards() );
                    if( message.getFromPlayerHand() )
                    {
                        mPlayers.get( originalSenderID ).removeCards( message.getCards() );
                    }
                    break;
            }
        }
    }

    @Override
    public synchronized void onDeviceConnect( String deviceID, String deviceName )
    {
        CardHolder newPlayer = mLeftPlayers.remove( deviceID );
        if( newPlayer != null )
        {
            if( newPlayer.getCardCount() > 0 )
            {
                this.sendCards( MOCK_SERVER_ADDRESS, newPlayer.getID(), newPlayer.getCards(), false );
            }
        }
        else
        {
            newPlayer = new CardHolder( deviceID, deviceName );
        }

        if( mPlayers.size() > 0 )
        {
            // Send the new player information about already connected players
            final CardHolder[] players = mPlayers.values().toArray( new CardHolder[ mPlayers.size() ] );
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CURRENT_PLAYERS, MOCK_SERVER_ADDRESS, newPlayer.getID() );
            message.putCurrentPlayers( players );
            this.sendMessageToDevice( message, MOCK_SERVER_ADDRESS, newPlayer.getID() );

            final GameMessage newPlayerMessage = new GameMessage( GameMessage.MessageType.MESSAGE_NEW_PLAYER, deviceID, null );
            newPlayerMessage.putName( deviceName );

            // Send new player to all connected remote players
            for( CardHolder player : mPlayers.values() )
            {
                if( !player.getID().equals( getLocalPlayerID() ) )
                {
                    newPlayerMessage.setReceiverID( player.getID() );
                    this.sendMessageToDevice( newPlayerMessage, MOCK_SERVER_ADDRESS, player.getID() );
                }
            }

            if( !newPlayer.getID().equals( getLocalPlayerID() ) )
            {
                // Send new player to local player
                newPlayerMessage.setReceiverID( getLocalPlayerID() );
                this.sendMessageToDevice( newPlayerMessage, MOCK_SERVER_ADDRESS, getLocalPlayerID() );
            }
        }

        mPlayers.put( deviceID, new CardHolder( deviceID, deviceName ) );
    }

    @Override
    public synchronized void onConnectionLost( String deviceID )
    {
        mLeftPlayers.put( deviceID, mPlayers.remove( deviceID ) );
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_PLAYER_LEFT, deviceID, null );

        // Send player left to all connected remote players
        for( CardHolder player : mPlayers.values() )
        {
            if( !player.getID().equals( getLocalPlayerID() ) )
            {
                message.setReceiverID( player.getID() );
                mConnection.sendDataToDevice( player.getID(), GameMessage.serializeMessage( message ) );
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
            for( GameConnectionListener listener : mListeners )
            {
                listener.onServerConnect( MOCK_SERVER_ADDRESS, MOCK_SERVER_NAME );
            }
        }
    }

    @Override
    public boolean saveGame( Context context, String saveName )
    {
        return GameSave.saveGame( context, new GameSave( saveName ), mPlayers.values().toArray( new CardHolder[ mPlayers.size() ] ), mLeftPlayers.values().toArray( new CardHolder[ mLeftPlayers.size() ] ) );
    }

    @Override
    public boolean openGameSave( Context context, GameSave gameSave )
    {
        final HashMap< String, CardHolder > players = new HashMap< String, CardHolder >();

        if( GameSave.openGameSave( context, gameSave, players, players ) )
        {
            CardHolder[] currentPlayers = mPlayers.values().toArray( new CardHolder[ mPlayers.size() ] );

            for( CardHolder player : currentPlayers )
            {
                this.clearPlayerHand( MOCK_SERVER_ADDRESS, player.getID() );
            }

            mPlayers.clear();
            mLeftPlayers = players;

            for( CardHolder player : currentPlayers )
            {
                this.onDeviceConnect( player.getID(), player.getName() );
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void sendMessageToDevice( GameMessage message, String senderID, String receiverID )
    {
        if( receiverID.equals( getLocalPlayerID() ) || receiverID.equals( MOCK_SERVER_ADDRESS ) )
        {
            final GameConnectionListener listener = this.findAppropriateListener( message.getMessageType(), senderID, receiverID );
            this.onMessageHandle( listener, senderID, receiverID, message );
        }
        else
        {
            final byte[] data = GameMessage.serializeMessage( message );
            mConnection.sendDataToDevice( receiverID, data );
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
        if( removingFromHand )
        {
            mPlayers.get( senderID ).removeCard( card );
        }
        mPlayers.get( receiverID ).addCard( card );

        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD, senderID, receiverID );
        message.putCard( card, removingFromHand );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    @Override
    public void sendCards( String senderID, String receiverID, Card[] cards, boolean removingFromHand )
    {
        if( removingFromHand )
        {
            mPlayers.get( senderID ).removeCards( cards );
        }
        CardHolder receiver = mPlayers.get( receiverID );
        if( receiver != null )
        {
            receiver.addCards( cards );
        }

        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARDS, senderID, receiverID );
        message.putCards( cards, removingFromHand );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    @Override
    public void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID )
    {
        CardHolder player = mPlayers.get( toBeClearedDeviceID );
        if( player != null )
        {
            player.clearCards();
        }

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
