package com.adamnickle.deck;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.DeckSettings;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Game.GameSaveIO;
import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class GameFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    private int mLastOrientation;
    private CardDisplayLayout mCardDisplay;
    private GameConnection mGameConnection;

    private CardHolder mLocalPlayer;
    private HashMap< String, CardHolder > mCardHolders;
    private ArrayList< CardHolder > mPlayers;

    private CardCollection mDeck;
    private boolean mHasToldToStart;

    private SlidingFrameLayout mSlidingTableLayout;

    public GameFragment()
    {
        mCardHolders = new HashMap< String, CardHolder >();
        mPlayers = new ArrayList< CardHolder >();
        mDeck = new CardCollection();
        mHasToldToStart = false;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );

        mSlidingTableLayout = (SlidingFrameLayout) getActivity().findViewById( R.id.table );
    }

    @Override
    public void onAttach( Activity activity )
    {
        super.onAttach( activity );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedStateInstance )
    {
        if( mCardDisplay == null )
        {
            mCardDisplay = new CardDisplayLayout( getActivity() )
            {
                @Override
                public void onBackgroundDown( MotionEvent event )
                {
                    if( mSlidingTableLayout != null )
                    {
                        mSlidingTableLayout.collapseFrame();
                    }
                }

                @Override
                public void onBackgroundDoubleTap( MotionEvent event )
                {
                    new AlertDialog.Builder( getContext() )
                            .setTitle( "Pick background" )
                            .setItems( R.array.backgrounds, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick( DialogInterface dialogInterface, int index )
                                {
                                    final String[] backgroundNames = getResources().getStringArray( R.array.backgrounds );
                                    final String backgroundName = backgroundNames[ index ];
                                    final int backgroundResource = DeckSettings.getBackgroundResourceFromString( getResources(), backgroundName );
                                    
                                    PreferenceManager
                                            .getDefaultSharedPreferences( getContext().getApplicationContext() )
                                            .edit()
                                            .putString( DeckSettings.BACKGROUND, backgroundName )
                                            .apply();
                                    setBackgroundResource( backgroundResource );
                                }
                            } )
                            .show();
                }

                @Override
                public void onCardSingleTap( MotionEvent event, PlayingCardView playingCardView )
                {
                    playingCardView.flip();
                }

                @Override
                public void onCardScroll( MotionEvent event1, MotionEvent event2, float distanceX, float distanceY, PlayingCardView playingCardView )
                {
                    super.onCardScroll( event1, event2, distanceX, distanceY, playingCardView );

                    if( mSlidingTableLayout.isOpen() && playingCardView.getBottom() < ( mSlidingTableLayout.getBottom() - mSlidingTableLayout.getPaddingBottom() ) )
                    {
                        mGameConnection.sendCard( playingCardView.getOwnerID(), TableFragment.TABLE_ID, playingCardView.getCard(), playingCardView.getOwnerID() );
                    }
                }
            };

            final String backgroundName = PreferenceManager
                    .getDefaultSharedPreferences( getActivity() )
                    .getString( DeckSettings.BACKGROUND, "White" );
            final int backgroundResource = DeckSettings.getBackgroundResourceFromString( getResources(), backgroundName );
            mCardDisplay.setBackgroundResource( backgroundResource );

            mCardDisplay.setGameUiListener( this );

            if( mLocalPlayer != null )
            {
                mLocalPlayer.setCardHolderListener( mCardDisplay.getCardHolderListener() );
            }

            mLastOrientation = getResources().getConfiguration().orientation;
        }
        else
        {
            ( (ViewGroup) mCardDisplay.getParent() ).removeView( mCardDisplay );

            final int newOrientation = getResources().getConfiguration().orientation;
            if( newOrientation != mLastOrientation )
            {
                mCardDisplay.onOrientationChange();
                mLastOrientation = newOrientation;
            }
        }

        return mCardDisplay;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if( !mHasToldToStart && !mGameConnection.isGameStarted() )
        {
            mHasToldToStart = true;
            mGameConnection.startGame();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Crouton.clearCroutonsForActivity( getActivity() );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.game, menu );

        if( mGameConnection.isServer() )
        {
            inflater.inflate( R.menu.game_server, menu );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.actionDealCards:
                handleDealCardsClick();
                return true;

            case R.id.actionClearPlayerHands:
                handleClearPlayerHandsClick();
                return true;

            case R.id.actionDealSingleCard:
                handleDealSingleCardClick();
                return true;

            case R.id.shuffleCards:
                mDeck.shuffle();
                return true;

            case R.id.actionLayoutCards:
                handleLayoutCardsClick();
                return true;

            case R.id.actionSaveGame:
                handleSaveGameClick();
                return true;

            case R.id.actionOpenGame:
                handleOpenGameClick();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void handleDealCardsClick()
    {
        if( mDeck.getCardCount() < mCardHolders.size() )
        {
            DialogHelper.showPopup( getActivity(), "No Cards Left", "There are not enough cards left to evenly deal to players." );
        }
        else
        {
            final CardHolder[] players = mPlayers.toArray( new CardHolder[ mPlayers.size() ] );
            final int maxCardsPerPlayer = mDeck.getCardCount() / players.length;
            final Integer[] cardsDealAmounts = new Integer[ maxCardsPerPlayer ];
            for( int i = 1; i <= maxCardsPerPlayer; i++ )
            {
                cardsDealAmounts[ i - 1 ] = i;
            }
            DialogHelper.createSelectItemDialog( getActivity(), "Number of cards to deal to each player:", cardsDealAmounts, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialogInterface, int index )
                {
                    int cardsPerPlayer = cardsDealAmounts[ index ];
                    final Card[][] cardsDealt = new Card[ players.length ][ cardsPerPlayer ];
                    for( int i = 0; i < cardsPerPlayer && mDeck.getCardCount() > 0; i++ )
                    {
                        for( int j = 0; j < players.length; j++ )
                        {
                            cardsDealt[ j ][ i ] = mDeck.removeTopCard();
                        }
                    }

                    for( int i = 0; i < cardsDealt.length; i++ )
                    {
                        mGameConnection.sendCards( mLocalPlayer.getID(), players[ i ].getID(), cardsDealt[ i ] );
                    }
                }
            } ).show();
        }
    }

    private void handleClearPlayerHandsClick()
    {
        for( CardHolder player : mCardHolders.values() )
        {
            mGameConnection.clearCards( mLocalPlayer.getID(), player.getID() );
        }
        mDeck.resetCards();
    }

    private void handleDealSingleCardClick()
    {
        if( mCardHolders.size() == 0 )
        {
            DialogHelper.showPopup( getActivity(), "No Players Connected", "There are not players connected to the current game to select from." );
        }
        else if( mDeck.getCardCount() == 0 )
        {
            DialogHelper.showPopup( getActivity(), "No Cards Left", "There are no cards left to deal." );
        }
        else
        {
            final CardHolder[] players = mPlayers.toArray( new CardHolder[ mPlayers.size() ] );
            final String[] playerNames = new String[ players.length ];
            final String[] playerIDs = new String[ players.length ];
            for( int i = 0; i < players.length; i++ )
            {
                playerNames[ i ] = players[ i ].getName();
                playerIDs[ i ] = players[ i ].getID();
            }
            DialogHelper.createSelectItemDialog( getActivity(), "Select player to deal card to:", playerNames, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialogInterface, int index )
                {
                    final String playerID = playerIDs[ index ];
                    mGameConnection.sendCard( mLocalPlayer.getID(), playerID, mDeck.removeTopCard(), null );
                    dialogInterface.dismiss();
                }
            } ).show();
        }
    }

    private void handleLayoutCardsClick()
    {
        if( mCardDisplay.getChildCount() == 0 )
        {
            DialogHelper
                    .createBlankAlertDialog( getActivity(), "No cards to layout." )
                    .setMessage( "You do not have any cards to layout." )
                    .setPositiveButton( "Close", null )
                    .show();
        }
        else
        {
            DialogHelper.createSelectItemDialog( getActivity(), "Select layout:", new String[]{ "By Rank", "By Suit" }, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialogInterface, int i )
                {
                    if( i == 0 )
                    {
                        mCardDisplay.sortCards( mLocalPlayer.getID(), CardCollection.SortingType.SORT_BY_RANK );
                    }
                    else if( i == 1 )
                    {
                        mCardDisplay.sortCards( mLocalPlayer.getID(), CardCollection.SortingType.SORT_BY_SUIT );
                    }
                    mCardDisplay.layoutCards( mLocalPlayer.getID() );
                }
            } ).show();
        }
    }

    private void handleSaveGameClick()
    {
        if( mGameConnection.isServer() )
        {
                DialogHelper.createEditTextDialog( getActivity(), "Enter Deck game save name:", "Game Save", "OK", "Cancel", new DialogHelper.OnEditTextDialogClickListener()
                {
                    @Override
                    public void onPositiveButtonClick( DialogInterface dialogInterface, String text )
                    {
                        if( mGameConnection.saveGame( getActivity().getApplicationContext(), text ) )
                        {
                            DialogHelper.displayNotification( getActivity(), "Game save successful.", Style.CONFIRM );
                        }
                        else
                        {
                            DialogHelper.displayNotification( getActivity(), "Game save not successful.", Style.ALERT );
                        }
                    }
                } ).show();
        }
    }

    private void handleOpenGameClick()
    {
        final AlertDialog dialog = DialogHelper
                .createBlankAlertDialog( getActivity(), "Select game save:" )
                .setPositiveButton( "Close", null )
                .create();

        final ListView gameSaveListView = GameSaveIO.getGameSaveListView( getActivity() );
        if( gameSaveListView != null )
        {
            gameSaveListView.setOnItemClickListener( new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick( AdapterView< ? > adapterView, View view, int i, long l )
                {
                    final File gameSaveFile = (File) adapterView.getItemAtPosition( i );
                    if( mGameConnection.openGameSave( getActivity(), gameSaveFile ) )
                    {
                        DialogHelper.displayNotification( getActivity(), "Game open successful.", Style.CONFIRM );
                    }
                    else
                    {
                        DialogHelper.displayNotification( getActivity(), "Game open not successful.", Style.ALERT );
                    }
                    dialog.dismiss();
                }
            } );
            gameSaveListView.getAdapter().registerDataSetObserver( new DataSetObserver()
            {
                @Override
                public void onChanged()
                {
                    if( gameSaveListView.getAdapter().getCount() == 0 )
                    {
                        if( dialog.isShowing() )
                        {
                            dialog.dismiss();
                            DialogHelper
                                    .createBlankAlertDialog( getActivity(), "Select game save:" )
                                    .setMessage( "There are no game saves to open." )
                                    .setPositiveButton( "Close", null )
                                    .show();
                        }
                    }
                }
            } );
            dialog.setView( gameSaveListView );
        }
        else
        {
            dialog.setMessage( "There are no game saves to open." );
        }
        dialog.show();
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( final String ownerID, final Card card )
    {
        if( this.canSendCard( mLocalPlayer.getID(), card ) )
        {
            final CardHolder[] players = mCardHolders.values().toArray( new CardHolder[ mCardHolders.size() ] );
            DialogHelper.createSelectItemDialog( getActivity(), "Select player to send card to:", players, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialogInterface, int i )
                {
                    final CardHolder player = players[ i ];
                    if( player != null )
                    {
                        mGameConnection.sendCard( ownerID, player.getID(), card, ownerID );
                    }
                    else
                    {
                        mCardDisplay.resetCard( mLocalPlayer.getID(), card );
                    }
                    dialogInterface.dismiss();
                }
            } ).setOnCancelListener( new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel( DialogInterface dialogInterface )
                {
                    mCardDisplay.resetCard( mLocalPlayer.getID(), card );
                    dialogInterface.dismiss();
                }
            } ).show();
            return true;
        }
        return false;
    }

    @Override
    public boolean canSendCard( String ownerID, Card card )
    {
        CardHolder player = mCardHolders.get( ownerID );
        return mCardHolders.size() > 1 && player != null && player.hasCard( card );
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public void setGameConnection( GameConnection gameConnection )
    {
        mGameConnection = gameConnection;
    }

    @Override
    public boolean canHandleMessage( GameMessage message )
    {
        return true;
    }

    @Override
    public void onCardHolderConnect( String ID, String name )
    {
        if( ID.startsWith( TableFragment.DRAW_PILE_ID_PREFIX ) )
        {
            return;
        }

        final CardHolder cardHolder = new CardHolder( ID, name );
        mCardHolders.put( ID, cardHolder );
        if( mGameConnection.isPlayerID( ID ) )
        {
            mPlayers.add( cardHolder );
            DialogHelper.displayNotification( getActivity(), name + " joined the game.", Style.CONFIRM );
        }
    }

    @Override
    public void onCardHolderNameReceive( String senderID, String newName )
    {
        final CardHolder player = mCardHolders.get( senderID );
        player.setName( newName );
        DialogHelper.displayNotification( getActivity(), player.getName() + " joined the game.", Style.CONFIRM );
    }

    @Override
    public void onCardHolderDisconnect( String ID )
    {
        if( mGameConnection.isGameStarted() )
        {
            CardHolder player = mCardHolders.remove( ID );
            mPlayers.remove( player );
            if( player != null )
            {
                DialogHelper.displayNotification( getActivity(), player.getName() + " left game.", Style.ALERT );
            }
        }
    }

    @Override
    public void onGameStarted()
    {
        mLocalPlayer = new CardHolder( mGameConnection.getLocalPlayerID(), "ME" );
        if( mCardDisplay != null )
        {
            mLocalPlayer.setCardHolderListener( mCardDisplay.getCardHolderListener() );
        }

        mCardHolders.put( mLocalPlayer.getID(), mLocalPlayer );
        mPlayers.add( mLocalPlayer );
    }

    @Override
    public void onServerConnect( String serverID, String serverName )
    {
        if( !mGameConnection.isServer() )
        {
            DialogHelper.displayNotification( getActivity(), "Connected to " + serverName + "'s server", Style.CONFIRM );
        }
        else
        {
            DialogHelper.displayNotification( getActivity(), "Server started", Style.CONFIRM );
        }

        String playerName = PreferenceManager
                .getDefaultSharedPreferences( getActivity().getApplicationContext() )
                .getString( DeckSettings.PLAYER_NAME, mGameConnection.getDefaultLocalPlayerName() );

        mGameConnection.sendCardHolderName( mLocalPlayer.getID(), GameConnection.MOCK_SERVER_ADDRESS, playerName );
    }

    @Override
    public void onServerDisconnect( String serverID )
    {
        Activity activity = getActivity();
        if( activity != null )
        {
            activity.setResult( GameActivity.RESULT_DISCONNECTED_FROM_SERVER, new Intent( GameActivity.class.getName() ) );
            activity.finish();
        }
    }

    @Override
    public void onNotification( String notification, Style style )
    {
        DialogHelper.displayNotification( getActivity(), notification, style );
    }

    @Override
    public void onConnectionStateChange( ConnectionFragment.State newState )
    {
        getActivity().runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                getActivity().invalidateOptionsMenu();
            }
        } );
    }

    @Override
    public void onCardReceive( String senderID, String receiverID, Card card )
    {
        if( receiverID.equals( mLocalPlayer.getID() ) )
        {
            mLocalPlayer.addCard( card );
        }
    }

    @Override
    public void onCardsReceive( String senderID, String receiverID, Card[] cards )
    {
        if( receiverID.equals( mLocalPlayer.getID() ) )
        {
            mLocalPlayer.addCards( cards );
        }
    }

    @Override
    public void onCardRemove( String removerID, String removedID, Card card )
    {
        if( removedID.equals( mLocalPlayer.getID() ) )
        {
            mCardHolders.get( removedID ).removeCard( card );
        }
    }

    @Override
    public void onCardsRemove( String removerID, String removedID, Card[] cards )
    {
        if( removedID.equals( mLocalPlayer.getID() ) )
        {
            mCardHolders.get( removedID ).removeCards( cards );
        }
    }

    @Override
    public void onClearCards( String commanderID, String commandeeID )
    {
        if( commandeeID.equals( mLocalPlayer.getID() ) )
        {
            mLocalPlayer.clearCards();
            String notification;
            if( commanderID.equals( mLocalPlayer.getID() ) )
            {
                notification = "You cleared your hand.";
            }
            else if( commanderID.equals( GameConnection.MOCK_SERVER_ADDRESS ) )
            {
                notification = "The server host cleared your hand.";
            }
            else
            {
                notification = mCardHolders.get( commanderID ).getName() + " cleared your hand.";
            }
            DialogHelper.displayNotification( getActivity(), notification, Style.INFO );
        }
    }

    @Override
    public void onReceiveCardHolders( String senderID, String receiverID, CardHolder[] cardHolders )
    {
        for( CardHolder cardHolder : cardHolders )
        {
            this.onCardHolderConnect( cardHolder.getID(), cardHolder.getName() );
        }
    }
}
