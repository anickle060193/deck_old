package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.TableFragment;
import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.Iterator;

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

    private void handleCardChanges( GameMessage message )
    {
        final CardHolder cardHolder = mPlayers.get( message.getReceiverID() );
        if( !message.Handled )
        {
            message.Handled = true;
            switch( message.getMessageType() )
            {
                case MESSAGE_RECEIVE_CARD:
                    final CardHolder removedFromCardHolder = mPlayers.get( message.getRemovedFromID() );
                    if( removedFromCardHolder != null )
                    {
                        this.removeCard( message.getOriginalSenderID(), removedFromCardHolder.getID(), message.getCard() );
                    }
                    cardHolder.addCard( message.getCard() );
                    break;

                case MESSAGE_RECEIVE_CARDS:
                    cardHolder.addCards( message.getCards() );
                    break;

                case MESSAGE_REMOVE_CARD:
                    cardHolder.removeCard( message.getCard() );
                    break;

                case MESSAGE_REMOVE_CARDS:
                    cardHolder.removeCards( message.getCards() );
                    break;

                case MESSAGE_CLEAR_CARDS:
                    cardHolder.clearCards();
                    break;
            }
        }
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public synchronized void onMessageHandle( GameConnectionListener listener, String originalSenderID, String receiverID, GameMessage message )
    {
        switch( message.getMessageType() )
        {
            case MESSAGE_RECEIVE_CARD:
                final String cardHolderID = message.getRemovedFromID();
                if( cardHolderID != null && !mPlayers.get( cardHolderID ).hasCard( message.getCard() ) )
                {
                    return;
                }
                break;
        }

        if( receiverID.equals( MOCK_SERVER_ADDRESS ) )
        {
            switch( message.getMessageType() )
            {
                case MESSAGE_SET_NAME:
                    final String newName = message.getPlayerName();
                    for( CardHolder player : mPlayers.values() )
                    {
                        if( player.getID().equals( originalSenderID ) )
                        {
                            player.setName( newName );
                        }
                        else
                        {
                            this.sendCardHolderName( originalSenderID, player.getID(), newName );
                        }
                    }
                    break;

                case MESSAGE_RECEIVE_CARD:
                    //TODO Server received card from player
                    break;

                case MESSAGE_CARD_HOLDERS:
                    final CardHolder[] cardHolders = message.getCardHolders();
                    for( CardHolder cardHolder : mPlayers.values() )
                    {
                        this.sendCardHolders( MOCK_SERVER_ADDRESS, cardHolder.getID(), cardHolders );
                    }
                    break;
            }
        }
        else if( receiverID.equals( getLocalPlayerID() ) )
        {
            super.onMessageHandle( listener, originalSenderID, receiverID, message );
        }
        else if( receiverID.equals( TableFragment.TABLE_ID ) )
        {
            super.onMessageHandle( listener, originalSenderID, receiverID, message );

            for( CardHolder cardHolder : mPlayers.values() )
            {
                if( !cardHolder.getID().equals( TableFragment.TABLE_ID ) )
                {
                    this.sendMessageToDevice( message, originalSenderID, cardHolder.getID() );
                }
            }
        }
        else
        {
            mConnection.sendDataToDevice( receiverID, GameMessage.serializeMessage( message ) );
        }

        this.handleCardChanges( message );
    }

    @Override
    public synchronized void onDeviceConnect( String deviceID, String deviceName )
    {
        CardHolder newPlayer = mLeftPlayers.remove( deviceID );
        if( newPlayer == null )
        {
            newPlayer = new CardHolder( deviceID, deviceName );
        }

        if( mPlayers.size() > 0 )
        {
            // Send the new player information about already connected players
            final CardHolder[] players = mPlayers.values().toArray( new CardHolder[ mPlayers.size() ] );
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_HOLDERS, MOCK_SERVER_ADDRESS, newPlayer.getID() );
            message.putCardHolders( players );
            this.sendMessageToDevice( message, MOCK_SERVER_ADDRESS, newPlayer.getID() );

            final GameMessage newPlayerMessage = new GameMessage( GameMessage.MessageType.MESSAGE_NEW_PLAYER, deviceID, null );
            newPlayerMessage.putName( deviceName );

            // Send new player to all connected remote players
            for( CardHolder player : mPlayers.values() )
            {
                if( !player.getID().equals( getLocalPlayerID() ) )
                {
                    newPlayerMessage.setReceiverID( player.getID() );
                    this.sendMessageToDevice( newPlayerMessage, deviceID, player.getID() );
                }
            }

            if( !newPlayer.getID().equals( getLocalPlayerID() ) )
            {
                // Send new player to local player
                newPlayerMessage.setReceiverID( getLocalPlayerID() );
                this.sendMessageToDevice( newPlayerMessage, deviceID, getLocalPlayerID() );
            }
        }

        mPlayers.put( deviceID, newPlayer );

        if( newPlayer.getCardCount() > 0 )
        {
            Card[] cards = newPlayer.getCards();
            newPlayer.clearCards();
            this.sendCards( MOCK_SERVER_ADDRESS, newPlayer.getID(), cards );
        }
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
        }
    }

    @Override
    public boolean saveGame( Context context, String saveName )
    {
        return GameSave.saveGame(
                context,
                new GameSave( saveName ),
                mPlayers.values().toArray( new CardHolder[ mPlayers.size() ] ),
                mLeftPlayers.values().toArray( new CardHolder[ mLeftPlayers.size() ] )
        );
    }

    @Override
    public boolean openGameSave( Context context, GameSave gameSave )
    {
        final HashMap< String, CardHolder > players = new HashMap< String, CardHolder >();

        if( GameSave.openGameSave( context, gameSave, players, players ) )
        {
            for( CardHolder player : mPlayers.values() )
            {
                this.clearCards( MOCK_SERVER_ADDRESS, player.getID() );
            }
            mLeftPlayers.clear();

            Iterator<CardHolder> cardHolderIterator = players.values().iterator();
            while( cardHolderIterator.hasNext() )
            {
                CardHolder cardHolder = cardHolderIterator.next();
                if( mPlayers.containsKey( cardHolder.getID() ) )
                {
                    this.sendCards( MOCK_SERVER_ADDRESS, cardHolder.getID(), cardHolder.getCards() );
                    cardHolderIterator.remove();
                }
            }

            mLeftPlayers = players;
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
        Crashlytics.log( "SENDING: " + message.toString() );

        if( receiverID.equals( getLocalPlayerID() ) || receiverID.equals( MOCK_SERVER_ADDRESS ) || receiverID.equals( TableFragment.TABLE_ID ) )
        {
            final GameConnectionListener listener = this.findAppropriateListener( message );
            this.onMessageHandle( listener, senderID, receiverID, message );
        }
        else
        {
            final byte[] data = GameMessage.serializeMessage( message );
            mConnection.sendDataToDevice( receiverID, data );
        }

        this.handleCardChanges( message );
    }
}
