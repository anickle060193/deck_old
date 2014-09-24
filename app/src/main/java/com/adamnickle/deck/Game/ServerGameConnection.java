package com.adamnickle.deck.Game;

import android.content.Context;

import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;

import java.util.Arrays;
import java.util.HashMap;

public class ServerGameConnection extends GameConnection
{
    private HashMap< String, Player > mPlayers;
    private HashMap< String, Player > mLeftPlayers;

    public ServerGameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        super( connection, listener );

        mPlayers = new HashMap< String, Player >();
        mLeftPlayers = new HashMap< String, Player >();
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
                    for( Player player : mPlayers.values() )
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
            super.onMessageReceive( senderID, bytes, allData );
        }
        else
        {
            // Otherwise, pass on to actual receiver device
            mConnection.sendDataToDevice( receiverID, data );

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
        Player newPlayer = mLeftPlayers.remove( deviceID );
        if( newPlayer != null )
        {
            if( newPlayer.getCardCount() > 0 )
            {
                this.sendCards( MOCK_SERVER_ADDRESS, newPlayer.getID(), newPlayer.getCards(), false );
            }
        }
        else
        {
            newPlayer = new Player( deviceID, deviceName );
        }

        if( mPlayers.size() > 0 )
        {
            // Send the new player information about already connected players
            final Player[] players = mPlayers.values().toArray( new Player[ mPlayers.size() ] );

            if( newPlayer.getID().equals( getLocalPlayerID() ) )
            {
                mListener.onReceiverCurrentPlayers( MOCK_SERVER_ADDRESS, newPlayer.getID(), players );
            }
            else
            {
                final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CURRENT_PLAYERS, MOCK_SERVER_ADDRESS, newPlayer.getID() );
                message.putCurrentPlayers( players );
                final byte[] data = GameMessage.serializeMessage( message );

                if( newPlayer.getID().equals( MOCK_SERVER_ADDRESS ) )
                {
                    this.onMessageReceive( MOCK_SERVER_ADDRESS, data.length, data );
                }
                else
                {
                    mConnection.sendDataToDevice( newPlayer.getID(), data );
                }
            }

            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_NEW_PLAYER, deviceID, null );
            message.putName( deviceName );

            // Send new player to all connected remote players
            for( Player player : mPlayers.values() )
            {
                if( !player.getID().equals( getLocalPlayerID() ) )
                {
                    message.setReceiverID( player.getID() );
                    mConnection.sendDataToDevice( player.getID(), GameMessage.serializeMessage( message ) );
                }
            }

            if( !newPlayer.getID().equals( getLocalPlayerID() ) )
            {
                // Send new player to local player
                message.setReceiverID( getLocalPlayerID() );
                final byte data[] = GameMessage.serializeMessage( message );
                this.onMessageReceive( deviceID, data.length, data );
            }
        }

        mPlayers.put( deviceID, new Player( deviceID, deviceName ) );
    }

    @Override
    public synchronized void onConnectionLost( String deviceID )
    {
        mLeftPlayers.put( deviceID, mPlayers.remove( deviceID ) );
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_PLAYER_LEFT, deviceID, null );

        // Send player left to all connected remote players
        for( Player player : mPlayers.values() )
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
            mListener.onServerConnect( MOCK_SERVER_ADDRESS, MOCK_SERVER_NAME );
        }
    }

    @Override
    public boolean saveGame( Context context, String saveName )
    {
        return GameSave.saveGame( context, new GameSave( saveName ), mPlayers.values().toArray( new Player[ mPlayers.size() ] ), mLeftPlayers.values().toArray( new Player[ mLeftPlayers.size() ] ) );
    }

    @Override
    public boolean openGameSave( Context context, GameSave gameSave )
    {
        final HashMap< String, Player > players = new HashMap< String, Player >();

        if( GameSave.openGameSave( context, gameSave, players, players ) )
        {
            Player[] currentPlayers = mPlayers.values().toArray( new Player[ mPlayers.size() ] );

            for( Player player : currentPlayers )
            {
                this.clearPlayerHand( MOCK_SERVER_ADDRESS, player.getID() );
            }

            mPlayers.clear();
            mLeftPlayers = players;

            for( Player player : currentPlayers )
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
    public void sendCard( String senderID, String receiverID, Card card, boolean removingFromHand )
    {
        if( removingFromHand )
        {
            mPlayers.get( senderID ).removeCard( card );
        }
        mPlayers.get( receiverID ).addCard( card );

        if( receiverID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardReceive( senderID, receiverID, card );
        }
        else
        {
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD, senderID, receiverID );
            message.putCard( card, removingFromHand );
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
    public void sendCards( String senderID, String receiverID, Card[] cards, boolean removingFromHand )
    {
        if( removingFromHand )
        {
            mPlayers.get( senderID ).removeCards( cards );
        }
        Player receiver = mPlayers.get( receiverID );
        if( receiver != null )
        {
            receiver.addCards( cards );
        }

        if( receiverID.equals( getLocalPlayerID() ) )
        {
            mListener.onCardsReceive( senderID, receiverID, cards );
        }
        else
        {
            final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARDS, senderID, receiverID );
            message.putCards( cards, removingFromHand );
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
        Player player = mPlayers.get( toBeClearedDeviceID );
        if( player != null )
        {
            player.clearHand();
        }

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
                this.onMessageReceive( setterID, data.length, data  );
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
