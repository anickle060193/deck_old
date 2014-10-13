package com.adamnickle.deck;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.adamnickle.deck.Interfaces.GameUiView;

import de.keyboardsurfer.android.widget.crouton.Style;

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

    @Override
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
        if( mTable != null )
        {
            mTable.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }

        return mTableView;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.table, menu );
    }

    @Override
    public boolean onAttemptSendCard( String senderID, Card card )
    {
        if( this.canSendCard( senderID, card ) )
        {
            mGameConnection.sendCard( senderID, mGameConnection.getLocalPlayerID(), card, senderID );
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

    }

    @Override
    public void onServerConnect( String deviceID, String deviceName )
    {
        mTable = new CardHolder( TABLE_ID, TABLE_NAME );
        if( mGameUiView != null )
        {
            mTable.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }
        if( mGameConnection.isServer() )
        {
            mGameConnection.sendCardHolderName( TABLE_ID, GameConnection.MOCK_SERVER_ADDRESS, TABLE_NAME );
        }
    }

    @Override
    public void onServerDisconnect( String deviceID )
    {

    }

    @Override
    public void onNotification( String notification, Style style )
    {

    }

    @Override
    public void onConnectionStateChange( ConnectionFragment.State newState )
    {

    }

    @Override
    public void onCardReceive( String senderID, String receiverID, Card card )
    {
        if( receiverID.equals( mTable.getID() ) )
        {
            mTable.addCard( card );
        }
    }

    @Override
    public void onCardsReceive( String senderID, String receiverID, Card[] cards )
    {
        if( receiverID.equals( mTable.getID() ) )
        {
            mTable.addCards( cards );
        }
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
    public void onCardsRemove( String removerID, String removedID, Card[] cards )
    {
        if( removedID.equals( mTable.getID() ) )
        {
            mTable.removeCards( cards );
        }
    }

    @Override
    public void onClearCards( String commanderID, String commandeeID )
    {
        if( commandeeID.equals( mTable.getID() ) )
        {
            mTable.clearCards();
        }
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
