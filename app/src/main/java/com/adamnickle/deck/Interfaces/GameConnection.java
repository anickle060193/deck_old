package com.adamnickle.deck.Interfaces;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.GameActivity;

import java.io.File;
import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

import de.keyboardsurfer.android.widget.crouton.Style;
import ru.noties.debug.Debug;

public abstract class GameConnection implements ConnectionListener
{
    public static final String MOCK_SERVER_ADDRESS = "mock_server_address";
    public static final String MOCK_SERVER_NAME = "Server Host";

    protected final ConnectionFragment mConnectionFragment;
    protected final LinkedList<GameConnectionListener> mListeners;

    private final ArrayBlockingQueue<GameMessage> mMessages;
    private final MessageHandlingThread mMessageHandlingThread;

    public GameConnection( ConnectionFragment connectionFragment )
    {
        mConnectionFragment = connectionFragment;
        mListeners = new LinkedList< GameConnectionListener >();
        mMessages = new ArrayBlockingQueue< GameMessage >( 10 );

        mMessageHandlingThread = new MessageHandlingThread();
    }

    private class MessageHandlingThread extends Thread
    {
        private boolean mHandling = true;

        @Override
        public void run()
        {
            while( mHandling )
            {
                try
                {
                    final GameMessage message = mMessages.take();
                    Debug.d( "HANDLING MESSAGE: " + message.getMessageType().name() );
                    final String originalSenderID = message.getOriginalSenderID();
                    final String receiverID = message.getReceiverID();
                    final GameConnectionListener listener = findAppropriateListener( message );
                    if( listener != null )
                    {
                        GameConnection.this.onMessageHandle( listener, originalSenderID, receiverID, message );
                    }
                }
                catch( InterruptedException e )
                {
                    Debug.e( e );
                }
            }
        }

        public void cancel()
        {
            mHandling = false;
            this.interrupt();
        }
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

    public boolean isPlayerID( String ID )
    {
        return mConnectionFragment.isPlayerID( ID );
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public final void onMessageReceive( String senderID, int bytes, byte[] allData )
    {
        final byte[] data = Arrays.copyOf( allData, bytes );
        final GameMessage message;
        try
        {
            message = GameMessage.deserializeMessage( data );
        }
        catch( InvalidClassException e )
        {
            if( !isServer() )
            {
                final Activity activity = mConnectionFragment.getActivity();
                activity.setResult( GameActivity.RESULT_INVALID_VERSIONS, new Intent( GameActivity.class.getName() ) );
                activity.finish();
            }
            return;
        }

        try
        {
            Debug.d( "RECEIVED MESSAGE: " + message.getMessageType().name() );
            mMessages.put( message );
        }
        catch( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageHandle( GameConnectionListener listener, String originalSenderID, String receiverID, GameMessage message )
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

            case MESSAGE_CLEAR_CARDS:
            {
                listener.onClearCards( originalSenderID, receiverID );
                break;
            }

            case MESSAGE_CARD_HOLDERS:
            {
                final CardHolder[] players = message.getCardHolders();
                for( GameConnectionListener listener2 : mListeners )
                {
                    listener2.onReceiveCardHolders( originalSenderID, receiverID, players );
                }
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

            case MESSAGE_GAME_OPEN:
            {
                final Card[] cards = message.getCards();
                listener.onGameOpen( originalSenderID, receiverID, cards );
                break;
            }
        }
    }

    @Override
    public void onNotification( String notification, Style style )
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onNotification( notification, style );
        }
    }

    @Override
    public void onConnectionStarted()
    {
        for( GameConnectionListener listener : mListeners )
        {
            listener.onGameStarted();
        }
    }

    @Override
    public void onConnectionStateChange( ConnectionFragment.State newState )
    {
        if( newState == ConnectionFragment.State.NONE )
        {
            if( mMessageHandlingThread.isAlive() )
            {
                Debug.d( "THREAD CANCELLED" );
                mMessageHandlingThread.cancel();
            }
        }
        else
        {
            if( !mMessageHandlingThread.isAlive() )
            {
                Debug.d( "THREAD STARTED" );
                try
                {
                    mMessageHandlingThread.start();
                }
                catch( IllegalThreadStateException e )
                {
                    Debug.d( "Thread already started." );
                }
            }
        }

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
            listener.onNotification( "Could not connect.", Style.CONFIRM );
        }
    }

    /*******************************************************************
     * GameConnection Methods
     *******************************************************************/
    public abstract void startGame();
    public abstract boolean saveGame( Context context, String saveName );
    public abstract boolean openGameSave( Context context, File gameSave );
    public abstract void sendMessageToDevice( GameMessage message, String senderID, String receiverID );

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

    public void sendGameOpen( String senderID, String receiverID, Card[] cards )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_GAME_OPEN, senderID, receiverID );
        message.putCards( cards );
        this.sendMessageToDevice( message, senderID, receiverID );
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
