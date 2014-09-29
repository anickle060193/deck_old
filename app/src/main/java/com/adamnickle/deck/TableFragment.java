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

import java.util.HashMap;

public class TableFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    private GameConnection mGameConnection;
    private GameUiView mGameUiView;
    private TableView mTableView;
    private HashMap< String, CardHolder > mCardHolders;

    public TableFragment()
    {
        mCardHolders = new HashMap< String, CardHolder >();
    }

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
        final CardHolder cardHolder = mCardHolders.get( senderID );
        return cardHolder != null && cardHolder.hasCard( card );
    }

    @Override
    public void setGameUiInterface( GameUiView gameUiView )
    {
        mGameUiView = gameUiView;
        for( CardHolder cardHolder : mCardHolders.values() )
        {
            cardHolder.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }
    }

    @Override
    public void setGameConnection( GameConnection gameConnection )
    {
        mGameConnection = gameConnection;
    }

    @Override
    public boolean canHandleMessage( GameMessage.MessageType messageType, String senderID, String receiverID )
    {
        return receiverID.startsWith( "card_pile_" );
    }

    @Override
    public void onCardHolderConnect( String ID, String name )
    {
        mCardHolders.put( ID, new CardHolder( ID, name ) );
    }

    @Override
    public void onCardHolderNameReceive( String senderID, String newName )
    {
        mCardHolders.get( senderID ).setName( newName );
    }

    @Override
    public void onCardHolderDisconnect( String ID )
    {
        mCardHolders.remove( ID );
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
        mCardHolders.get( receiverID ).addCard( card );
    }

    @Override
    public void onCardsReceive( String senderID, String receiverID, Card[] cards )
    {
        mCardHolders.get( receiverID ).addCards( cards );
    }

    @Override
    public void onCardRequested( String requesterID, String requesteeID )
    {

    }

    @Override
    public void onClearCards( String commanderID, String commandeeID )
    {
        mCardHolders.get( commandeeID ).clearCards();
    }

    @Override
    public void onSetDealer( String setterID, String setID, boolean isDealer )
    {

    }

    @Override
    public void onReceiverCurrentPlayers( String senderID, String receiverID, CardHolder[] players )
    {

    }
}
