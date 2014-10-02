package com.adamnickle.deck;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.adamnickle.deck.Interfaces.GameUiView;

public class TableFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    public static final String TABLE_ID = "table";
    public static final String TABLE_NAME = "Table";

    private GameConnection mGameConnection;
    private GameUiView mGameUiView;
    private TableView mTableView;
    private CardHolder mTable;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mTableView == null )
        {
            mTableView = new TableView( getActivity() );
            mTableView.setGameUiListener( this );
        }
        else
        {
            container.removeView( mTableView );
        }

        this.setGameUiInterface( mTableView );
        return mTableView;
    }

    @Override
    public boolean onAttemptSendCard( String senderID, Card card )
    {
        if( this.canSendCard( senderID, card ) )
        {
            mGameConnection.sendCard( senderID, mGameConnection.getLocalPlayerID(), card, true );
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean canSendCard( String senderID, Card card )
    {
        return mTable.hasCard( card );
    }

    @Override
    public void setGameUiInterface( GameUiView gameUiView )
    {
        mGameUiView = gameUiView;
        if( mTable != null )
        {
            mTable.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }
    }

    @Override
    public void setGameConnection( GameConnection gameConnection )
    {
        mGameConnection = gameConnection;
    }

    @Override
    public boolean canHandleMessage( GameMessage message )
    {
        return message.getReceiverID().equals( TABLE_ID );
    }

    @Override
    public void onCardHolderConnect( String ID, String name )
    {
        mTable = new CardHolder( ID, name );
        if( mGameUiView != null )
        {
            mTable.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }
    }

    @Override
    public void onCardHolderNameReceive( String senderID, String newName )
    {

    }

    @Override
    public void onCardHolderDisconnect( String ID )
    {

    }

    @Override
    public void onGameStarted()
    {
        if( mGameConnection.isServer() )
        {
            mGameConnection.onDeviceConnect( TABLE_ID, TABLE_NAME );
        }
        else
        {
            this.onCardHolderConnect( TABLE_ID, TABLE_NAME );
        }
    }

    @Override
    public void onServerConnect( String deviceID, String deviceName )
    {

    }

    @Override
    public void onServerDisconnect( String deviceID )
    {

    }

    @Override
    public void onNotification( String notification )
    {

    }

    @Override
    public void onConnectionStateChange( Connection.State newState )
    {

    }

    @Override
    public void onCardReceive( String senderID, String receiverID, Card card )
    {
        mTable.addCard( card );
    }

    @Override
    public void onCardsReceive( String senderID, String receiverID, Card[] cards )
    {
        mTable.addCards( cards );
    }

    @Override
    public void onCardRemove( String removerID, String removedID, Card card )
    {
        if( removedID.equals( mTable.getID() ) )
        {
            mTable.removeCard( card );
        }
    }

    @Override
    public void onCardRequested( String requesterID, String requesteeID )
    {

    }

    @Override
    public void onClearCards( String commanderID, String commandeeID )
    {
        mTable.clearCards();
    }

    @Override
    public void onSetDealer( String setterID, String setID, boolean isDealer )
    {

    }

    @Override
    public void onReceiveCardHolders( String senderID, String receiverID, CardHolder[] cardHolders )
    {

    }
}
