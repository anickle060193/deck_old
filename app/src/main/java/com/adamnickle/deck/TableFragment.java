package com.adamnickle.deck;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiListener;

import de.keyboardsurfer.android.widget.crouton.Style;

public class TableFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    public static final String TABLE_ID = "table";
    public static final String TABLE_NAME = "Table";

    private static float FLING_VELOCITY = 400;

    private GameConnection mGameConnection;
    private CardDisplayLayout mTableView;
    private CardHolder mTable;

    private SlidingFrameLayout mSlidingTableLayout;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Override
    public void onAttach( Activity activity )
    {
        super.onAttach( activity );

        FLING_VELOCITY *= getResources().getDisplayMetrics().density;
        mSlidingTableLayout = (SlidingFrameLayout) activity.findViewById( R.id.table );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mTableView == null )
        {
            mTableView = new CardDisplayLayout( getActivity() )
            {
                @Override
                public PlayingCardView createPlayingCardView( String cardHolderID, Card card )
                {
                    PlayingCardView playingCardView = new PlayingCardView( getContext(), cardHolderID, card, 0.5f );
                    playingCardView.flip( true, false );
                    return playingCardView;
                }

                @Override
                public void onCardSingleTap( MotionEvent event, PlayingCardView playingCardView )
                {

                }

                @Override
                public void onBackgroundDoubleTap( MotionEvent event )
                {

                }

                @Override
                public void onBackgroundFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY )
                {
                    if( velocityY < -1.0f * FLING_VELOCITY && Math.abs( velocityX ) < FLING_VELOCITY )
                    {
                        mSlidingTableLayout.collapseFrame();
                    }
                }
            };
            mTableView.setGameUiListener( this );
            if( mTable != null )
            {
                mTable.setCardHolderListener( mTableView.getCardHolderListener() );
            }
        }
        else
        {
            container.removeView( mTableView );
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
        if( mTableView != null )
        {
            mTable.setCardHolderListener( mTableView.getCardHolderListener() );
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
    public void onReceiveCardHolders( String senderID, String receiverID, CardHolder[] cardHolders )
    {

    }
}
