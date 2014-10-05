package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.TableFragment;

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
                if( !cardHolder.getID().equals( TableFragment.TABLE_ID ) && !cardHolder.getID().equals( getLocalPlayerID() ) )
                {
                    this.sendMessageToDevice( message, originalSenderID, cardHolder.getID() );
                }
            }
        }
        else
        {
            // Otherwise, pass on to actual receiver device
            mConnection.sendDataToDevice( receiverID, GameMessage.serializeMessage( message ) );
        }

        switch( message.getMessageType() )
        {
            case MESSAGE_RECEIVE_CARD:
                mPlayers.get( receiverID ).addCard( message.getCard() );
                break;

            case MESSAGE_RECEIVE_CARDS:
                mPlayers.get( receiverID ).addCards( message.getCards() );
                break;

            case MESSAGE_REMOVE_CARD:
                mPlayers.get( originalSenderID ).removeCard( message.getCard() );
                break;

            case MESSAGE_REMOVE_CARDS:
                mPlayers.get( originalSenderID ).removeCards( message.getCards() );
                break;

            case MESSAGE_CLEAR_CARDS:
                for( CardHolder cardHolder : mPlayers.values() )
                {
                    cardHolder.clearCards();
                }
                break;
        }
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
            this.sendCards( MOCK_SERVER_ADDRESS, newPlayer.getID(), newPlayer.getCards() );
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
            CardHolder[] currentPlayers = mPlayers.values().toArray( new CardHolder[ mPlayers.size() ] );

            for( CardHolder player : currentPlayers )
            {
                this.clearCards( MOCK_SERVER_ADDRESS, player.getID() );
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
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card )
    {
        mPlayers.get( receiverID ).addCard( card );

        super.sendCard( senderID, receiverID, card );
    }

    @Override
    public void sendCards( String senderID, String receiverID, Card[] cards )
    {
        mPlayers.get( receiverID ).addCards( cards );

        super.sendCards( senderID, receiverID, cards );
    }

    @Override
    public void clearCards( String commandingDeviceID, String toBeClearedDeviceID )
    {
        mPlayers.get( toBeClearedDeviceID ).clearCards();

        super.clearCards( commandingDeviceID, toBeClearedDeviceID );
    }

    @Override
    public void setDealer( String setterID, String setteeID, boolean isDealer )
    {
        //TODO do something

        super.setDealer( setterID, setteeID, isDealer );
    }
}
