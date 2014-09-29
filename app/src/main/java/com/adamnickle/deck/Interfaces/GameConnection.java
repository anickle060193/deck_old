package com.adamnickle.deck.Interfaces;


import android.content.Context;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Game.GameSave;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class GameConnection implements ConnectionListener
{
    public static final String MOCK_SERVER_ADDRESS = "mock_server_address";
    public static final String MOCK_SERVER_NAME = "Server Host";

    protected Connection mConnection;
    protected LinkedList<GameConnectionListener> mListeners;

    public GameConnection( Connection connection )
    {
        mConnection = connection;
        mListeners = new LinkedList< GameConnectionListener >();
    }

    public void addGameConnectionListener( GameConnectionListener listener )
    {
        mListeners.addFirst( listener );
    }

    public GameConnectionListener findAppropriateListener( GameMessage.MessageType messageType, String senderID, String receiverID )
    {
        for( GameConnectionListener listener : mListeners )
        {
            if( listener.canHandleMessage( messageType, senderID, receiverID ) )
            {
                return listener;
            }
        }
        return null;
    }

    public boolean isServer()
    {
        return mConnection.getConnectionType() == Connection.ConnectionType.SERVER;
    }

    public boolean isGameStarted()
    {
        return mConnection.getState() != Connection.State.NONE;
    }

    public String getLocalPlayerID()
    {
        return mConnection.getLocalDeviceID();
    }

    public String getDefaultLocalPlayerName()
    {
        return mConnection.getLocalDeviceName();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public final void onMessageReceive( String senderID, int bytes, byte[] allData )
    {
        final byte[] data = Arrays.copyOf( allData, bytes );
        final GameMessage message = GameMessage.deserializeMessage( data );
        final String originalSenderID = message.getOriginalSenderID();
        final String receiverID = message.getReceiverID();
        final GameConnectionListener listener = findAppropriateListener( message.getMessageType(), originalSenderID, receiverID );
        if( listener != null )
        {
            onMessageHandle( listener, originalSenderID, receiverID, message );
        }
    }

    @Override
    public void onMessageHandle( GameConnectionListener listener, String originalSenderID, String receiverID, GameMessage message )
    {
        switch( message.getMessageType() )
        {
            case MESSAGE_NEW_PLAYER:
                listener.onCardHolderConnect( originalSenderID, message.getPlayerName() );
                break;

            case MESSAGE_SET_PLAYER_NAME:
                final String newName = message.getPlayerName();
                listener.onCardHolderNameReceive( originalSenderID, newName );
                break;

            case MESSAGE_PLAYER_LEFT:
                listener.onCardHolderDisconnect( originalSenderID );
                break;

            case MESSAGE_CARD:
                final Card card = message.getCard();
                listener.onCardReceive( originalSenderID, receiverID, card );
                break;

            case MESSAGE_CARDS:
                final Card[] cards = message.getCards();
                listener.onCardsReceive( originalSenderID, receiverID, cards );
                break;

            case MESSAGE_CARD_REQUEST:
                listener.onCardRequested( originalSenderID, receiverID ); //TODO Add something about valid cards
                break;

            case MESSAGE_CLEAR_HAND:
                listener.onClearCards( originalSenderID, receiverID );
                break;

            case MESSAGE_SET_DEALER:
                final boolean isDealer = message.getIsDealer();
                listener.onSetDealer( originalSenderID, receiverID, isDealer );
                break;

            case MESSAGE_CURRENT_PLAYERS:
                final CardHolder[] players = message.getCurrentPlayers();
                listener.onReceiverCurrentPlayers( originalSenderID, receiverID, players );
                break;
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
    public void onConnectionStateChange( Connection.State newState )
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
     * GameConnection Required Methods
     *******************************************************************/
    public abstract void startGame();
    public abstract boolean saveGame( Context context, String saveName );
    public abstract boolean openGameSave( Context context, GameSave gameSave );
    public abstract void sendMessageToDevice( GameMessage message, String senderID, String receiverID );
    public abstract void requestCard( String requesterID, String requesteeID );
    public abstract void sendCard( String senderID, String receiverID, Card card, boolean removingFromHand );
    public abstract void sendCards( String senderID, String receiverID, Card[] cards, boolean removingFromHand );
    public abstract void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID );
    public abstract void setDealer( String setterID, String setteeID, boolean isDealer );
    public abstract void sendPlayerName( String senderID, String receiverID, String name );
}
