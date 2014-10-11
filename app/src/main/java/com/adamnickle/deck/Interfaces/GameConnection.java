package com.adamnickle.deck.Interfaces;


import android.content.Context;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Game.GameSave;
import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class GameConnection implements ConnectionListener
{
    public static final String MOCK_SERVER_ADDRESS = "mock_server_address";
    public static final String MOCK_SERVER_NAME = "Server Host";

    protected ConnectionFragment mConnectionFragment;
    protected LinkedList<GameConnectionListener> mListeners;

    public GameConnection( ConnectionFragment connectionFragment )
    {
        mConnectionFragment = connectionFragment;
        mListeners = new LinkedList< GameConnectionListener >();
    }

    public void addGameConnectionListener( GameConnectionListener listener )
    {
        mListeners.addFirst( listener );
    }

    public GameConnectionListener findAppropriateListener( GameMessage message )
    {
        for( GameConnectionListener listener : mListeners )
        {
            if( listener.canHandleMessage( message ) )
            {
                return listener;
            }
        }
        return null;
    }

    public boolean isServer()
    {
        return mConnectionFragment.getConnectionType() == ConnectionFragment.ConnectionType.SERVER;
    }

    public boolean isGameStarted()
    {
        return mConnectionFragment.getState() != ConnectionFragment.State.NONE;
    }

    public String getLocalPlayerID()
    {
        return mConnectionFragment.getLocalDeviceID();
    }

    public String getDefaultLocalPlayerName()
    {
        return mConnectionFragment.getLocalDeviceName();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public synchronized final void onMessageReceive( String senderID, int bytes, byte[] allData )
    {
        final byte[] data = Arrays.copyOf( allData, bytes );
        final GameMessage message = GameMessage.deserializeMessage( data );

        Crashlytics.log( "RECEIVED: " + message.toString() );

        final String originalSenderID = message.getOriginalSenderID();
        final String receiverID = message.getReceiverID();
        final GameConnectionListener listener = findAppropriateListener( message );
        if( listener != null )
        {
            this.onMessageHandle( listener, originalSenderID, receiverID, message );
        }
    }

    @Override
    public synchronized void onMessageHandle( GameConnectionListener listener, String originalSenderID, String receiverID, GameMessage message )
    {
        switch( message.getMessageType() )
        {
            case MESSAGE_NEW_PLAYER:
            {
                listener.onCardHolderConnect( originalSenderID, message.getPlayerName() );
                break;
            }

            case MESSAGE_SET_NAME:
            {
                final String newName = message.getPlayerName();
                listener.onCardHolderNameReceive( originalSenderID, newName );
                break;
            }

            case MESSAGE_PLAYER_LEFT:
            {
                listener.onCardHolderDisconnect( originalSenderID );
                break;
            }

            case MESSAGE_RECEIVE_CARD:
            {
                final Card card = message.getCard();
                listener.onCardReceive( originalSenderID, receiverID, card );
                break;
            }

            case MESSAGE_RECEIVE_CARDS:
            {
                final Card[] cards = message.getCards();
                listener.onCardsReceive( originalSenderID, receiverID, cards );
                break;
            }

            case MESSAGE_CARD_REQUEST:
            {
                listener.onCardRequested( originalSenderID, receiverID ); //TODO Add something about valid cards
                break;
            }

            case MESSAGE_CLEAR_CARDS:
            {
                listener.onClearCards( originalSenderID, receiverID );
                break;
            }

            case MESSAGE_SET_DEALER:
            {
                final boolean isDealer = message.getIsDealer();
                listener.onSetDealer( originalSenderID, receiverID, isDealer );
                break;
            }

            case MESSAGE_CARD_HOLDERS:
            {
                final CardHolder[] players = message.getCardHolders();
                listener.onReceiveCardHolders( originalSenderID, receiverID, players );
                break;
            }

            case MESSAGE_REMOVE_CARD:
            {
                final Card card = message.getCard();
                listener.onCardRemove( originalSenderID, receiverID, card );
                break;
            }

            case MESSAGE_REMOVE_CARDS:
            {
                final Card[] cards = message.getCards();
                listener.onCardsRemove( originalSenderID, receiverID, cards );
                break;
            }
        }
    }

    @Override
    public void onNotification( String notification )
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onNotification( notification );
        }
    }

    @Override
    public void onConnectionStarted()
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onServerConnect( MOCK_SERVER_ADDRESS, MOCK_SERVER_NAME );
            listener.onGameStarted();
        }
    }

    @Override
    public void onConnectionStateChange( ConnectionFragment.State newState )
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onConnectionStateChange( newState );
        }
    }

    @Override
    public void onConnectionFailed()
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onNotification( "Could not connect." );
        }
    }

    /*******************************************************************
     * GameConnection Methods
     *******************************************************************/
    public abstract void startGame();
    public abstract boolean saveGame( Context context, String saveName );
    public abstract boolean openGameSave( Context context, GameSave gameSave );
    public abstract void sendMessageToDevice( GameMessage message, String senderID, String receiverID );

    public void requestCard( String requesterID, String requesteeID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_REQUEST, requesterID, requesteeID );
        this.sendMessageToDevice( message, requesterID, requesteeID );
    }

    public void sendCard( String senderID, String receiverID, Card card, String removedFromID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_RECEIVE_CARD, senderID, receiverID );
        message.putCard( card );
        message.putRemovedFromID( removedFromID );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    public void sendCards( String senderID, String receiverID, Card[] cards )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_RECEIVE_CARDS, senderID, receiverID );
        message.putCards( cards );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    public void removeCard( String removerID, String removedFromID, Card card )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_REMOVE_CARD, removerID, removedFromID );
        message.putCard( card );
        this.sendMessageToDevice( message, removerID, removedFromID );
    }

    public void removeCards( String removerID, String removedFromID, Card[] cards )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_REMOVE_CARDS, removerID, removedFromID );
        message.putCards( cards );
        this.sendMessageToDevice( message, removerID, removedFromID );
    }

    public void clearCards( String commandingDeviceID, String toBeClearedDeviceID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CLEAR_CARDS, commandingDeviceID, toBeClearedDeviceID );
        this.sendMessageToDevice( message, commandingDeviceID, toBeClearedDeviceID );
    }

    public void setDealer( String setterID, String setteeID, boolean isDealer )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_DEALER, setterID, setteeID );
        message.putIsDealer( isDealer );
        this.sendMessageToDevice( message, setterID, setteeID );
    }

    public void sendCardHolderName( String senderID, String receiverID, String name )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_SET_NAME, senderID, receiverID );
        message.putName( name );
        this.sendMessageToDevice( message, senderID, receiverID );
    }

    public void sendCardHolders( String senderID, String receiverID, CardHolder[] cardHolders )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_HOLDERS, senderID, receiverID );
        message.putCardHolders( cardHolders );
        this.sendMessageToDevice( message, senderID, receiverID );
    }
}
