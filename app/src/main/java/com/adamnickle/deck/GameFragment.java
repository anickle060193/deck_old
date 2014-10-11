package com.adamnickle.deck;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.DeckSettings;
import com.adamnickle.deck.Game.GameMessage;
import com.adamnickle.deck.Game.GameSave;
import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.adamnickle.deck.Interfaces.GameUiView;

import java.util.ArrayList;
import java.util.HashMap;

public class GameFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    private int mLastOrientation;
    private GameView mGameView;
    private GameUiView mGameUiView;
    private GameConnection mGameConnection;

    private CardHolder mLocalPlayer;
    private HashMap< String, CardHolder > mCardHolders;
    private ArrayList< CardHolder > mPlayers;

    private CardCollection mDeck;
    private boolean mIsDealer;

    public GameFragment()
    {
        mCardHolders = new HashMap< String, CardHolder >();
        mPlayers = new ArrayList< CardHolder >();
        mDeck = new CardCollection();
        mIsDealer = false;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedStateInstance )
    {
        if( mGameView == null )
        {
            mGameView = new GameView( getActivity() );
            mGameView.setGameUiListener( this );

            mLastOrientation = getResources().getConfiguration().orientation;
        }
        else
        {
            container.removeView( mGameView );

            final int newOrientation = getResources().getConfiguration().orientation;
            if( newOrientation != mLastOrientation )
            {
                mGameView.onOrientationChange();
                mLastOrientation = newOrientation;
            }
        }
        this.setGameUiInterface( mGameView );

        return mGameView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if( !mGameConnection.isGameStarted() )
        {
            mGameConnection.startGame();
        }
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.game, menu );

        if( mGameConnection.isServer() )
        {
            inflater.inflate( R.menu.game_server, menu );
        }

        if( mGameConnection.isServer() || mIsDealer )
        {
            inflater.inflate( R.menu.game_dealer, menu );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.setDealer:
            {
                final CardHolder players[] = mPlayers.toArray( new CardHolder[ mPlayers.size() ] );
                mGameUiView.createSelectItemDialog( "Select dealer:", players, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        CardHolder newDealer = players[ i ];
                        mGameConnection.setDealer( mLocalPlayer.getID(), newDealer.getID(), true );
                    }
                } ).show();
                return true;
            }

            case R.id.actionDealCards:
            {
                if( mDeck.getCardCount() < mCardHolders.size() )
                {
                    mGameUiView.showPopup( "No Cards Left", "There are not enough cards left to evenly deal to players." );
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
                    mGameUiView.createSelectItemDialog( "Number of cards to deal to each player:", cardsDealAmounts, new DialogInterface.OnClickListener()
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
                return true;
            }

            case R.id.actionClearPlayerHands:
            {
                for( CardHolder player : mCardHolders.values() )
                {
                    mGameConnection.clearCards( mLocalPlayer.getID(), player.getID() );
                }
                mDeck.resetCards();
                return true;
            }

            case R.id.actionDealSingleCard:
            {
                if( mCardHolders.size() == 0 )
                {
                    mGameUiView.showPopup( "No Players Connected", "There are not players connected to the current game to select from." );
                }
                else if( mDeck.getCardCount() == 0 )
                {
                    mGameUiView.showPopup( "No Cards Left", "There are no cards left to deal." );
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
                    mGameUiView.createSelectItemDialog( "Select player to deal card to:", playerNames, new DialogInterface.OnClickListener()
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
                return true;
            }

            case R.id.shuffleCards:
            {
                mDeck.shuffle();
                return true;
            }

            case R.id.actionLayoutCards:
            {
                if( mGameUiView != null )
                {
                    mGameUiView.createSelectItemDialog( "Select layout:", new String[]{ "By Rank", "By Suit" }, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            if( i == 0 )
                            {
                                mGameUiView.sortCards( mLocalPlayer.getID(), CardCollection.SortingType.SORT_BY_RANK );
                            }
                            else if( i == 1 )
                            {
                                mGameUiView.sortCards( mLocalPlayer.getID(), CardCollection.SortingType.SORT_BY_SUIT );
                            }
                            mGameUiView.layoutCards( mLocalPlayer.getID() );
                        }
                    } ).show();
                }
                return true;
            }

            case R.id.actionSaveGame:
            {
                if( mGameConnection.isServer() )
                {
                    if( mGameUiView != null )
                    {
                        mGameUiView.createEditTextDialog( "Enter Deck game save name:", "Game Save", "OK", "Cancel", new GameUiView.OnEditTextDialogClickListener()
                        {
                            @Override
                            public void onPositiveButtonClick( DialogInterface dialogInterface, String text )
                            {
                                if( mGameConnection.saveGame( getActivity().getApplicationContext(), text ) )
                                {
                                    mGameUiView.displayNotification( "Game save successful." );
                                }
                                else
                                {
                                    mGameUiView.displayNotification( "Game save not successful." );
                                }
                            }
                        } ).show();
                    }
                }
                return true;
            }

            case R.id.actionOpenGame:
            {
                if( mGameUiView != null )
                {
                    final GameSave[] gameSaves = GameSave.getGameSaves( getActivity().getApplicationContext() );

                    if( gameSaves.length == 0 )
                    {
                        mGameUiView.showPopup( "No Deck Game Saves", "There are currently no saved Deck games." );
                    }
                    else
                    {
                        final AlertDialog.Builder dialogBuilder = mGameUiView.createBlankAlertDialog( "Select game save:" );
                        final ListView gameSaveListView = GameSave.getGameSaveListView( getActivity(), gameSaves );
                        dialogBuilder.setView( gameSaveListView );
                        final AlertDialog dialog = dialogBuilder.create();
                        gameSaveListView.setOnItemClickListener( new AdapterView.OnItemClickListener()
                        {
                            @Override
                            public void onItemClick( AdapterView< ? > adapterView, View view, int i, long l )
                            {
                                if( mGameConnection.openGameSave( getActivity().getApplicationContext(), gameSaves[ i ] ) )
                                {
                                    mGameUiView.displayNotification( "Game open successful." );
                                }
                                else
                                {
                                    mGameUiView.displayNotification( "Game open not successful." );
                                }
                                dialog.dismiss();
                            }
                        } );
                        dialog.show();
                    }
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( final String senderID, final Card card )
    {
        if( this.canSendCard( mLocalPlayer.getID(), card ) )
        {
            final CardHolder[] players = mCardHolders.values().toArray( new CardHolder[ mCardHolders.size() ] );
            mGameUiView.createSelectItemDialog( "Select player to send card to:", players, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialogInterface, int i )
                {
                    final CardHolder player = players[ i ];
                    if( player != null )
                    {
                        mGameConnection.sendCard( senderID, player.getID(), card, senderID );
                    }
                    else
                    {
                        mGameUiView.resetCard( mLocalPlayer.getID(), card );
                    }
                    dialogInterface.dismiss();
                }
            } ).setOnCancelListener( new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel( DialogInterface dialogInterface )
                {
                    mGameUiView.resetCard( mLocalPlayer.getID(), card );
                    dialogInterface.dismiss();
                }
            } ).show();
            return true;
        }
        return false;
    }

    @Override
    public boolean canSendCard( String senderID, Card card )
    {
        CardHolder player = mCardHolders.get( senderID );
        return mCardHolders.size() > 1 && player != null && player.hasCard( card );
    }

    @Override
    public void setGameUiInterface( GameUiView gameUiView )
    {
        mGameUiView = gameUiView;
        if( mLocalPlayer != null )
        {
            mLocalPlayer.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }
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
        final CardHolder cardHolder = new CardHolder( ID, name );
        mCardHolders.put( ID, cardHolder );
        if( !ID.equals( TableFragment.TABLE_ID ) )
        {
            mPlayers.add( cardHolder );
            if( mGameUiView != null )
            {
                mGameUiView.displayNotification( name + " joined the game." );
            }
        }
    }

    @Override
    public void onCardHolderNameReceive( String senderID, String newName )
    {
        final CardHolder player = mCardHolders.get( senderID );
        player.setName( newName );
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( player.getName() + " joined the game." );
        }
    }

    @Override
    public void onCardHolderDisconnect( String ID )
    {
        if( mGameConnection.isGameStarted() )
        {
            CardHolder player = mCardHolders.remove( ID );
            mPlayers.remove( player );
            if( player != null && mGameUiView != null )
            {
                mGameUiView.displayNotification( player.getName() + " left game." );
            }
        }
    }

    @Override
    public void onGameStarted()
    {
        mLocalPlayer = new CardHolder( mGameConnection.getLocalPlayerID(), "ME" );
        if( mGameUiView != null )
        {
            mLocalPlayer.setCardHolderListener( mGameUiView.getCardHolderListener() );
        }

        String playerName = PreferenceManager
                .getDefaultSharedPreferences( getActivity().getApplicationContext() )
                .getString( DeckSettings.PLAYER_NAME, mGameConnection.getDefaultLocalPlayerName() );

        mCardHolders.put( mLocalPlayer.getID(), mLocalPlayer );
        mPlayers.add( mLocalPlayer );
        mGameConnection.onDeviceConnect( mGameConnection.getLocalPlayerID(), playerName );
    }

    @Override
    public void onServerConnect( String serverID, String serverName )
    {
        if( mGameUiView != null && !mGameConnection.isServer() )
        {
            mGameUiView.displayNotification( "Connected to " + serverName + "'s server" );
        }
    }

    @Override
    public void onServerDisconnect( String serverID )
    {
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( "Disconnected from server." );
        }
        Activity activity = getActivity();
        if( activity != null )
        {
            activity.finish();
        }
    }

    @Override
    public void onNotification( String notification )
    {
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( notification );
        }
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
            if( mGameUiView != null )
            {
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
                mGameUiView.displayNotification( notification );
            }
        }
    }

    @Override
    public void onSetDealer( String setterID, String setID, boolean isDealer )
    {
        if( setID.equals( mLocalPlayer.getID() ) )
        {
            mIsDealer = isDealer;
            if( mGameUiView != null )
            {
                String notification;
                if( mIsDealer )
                {
                    notification = mCardHolders.get( setterID ).getName() + " made you dealer.";
                }
                else
                {
                    notification = mCardHolders.get( setterID ).getName() + " unmade you dealer";
                }
                mGameUiView.displayNotification( notification );
            }
            getActivity().runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    getActivity().invalidateOptionsMenu();
                }
            } );
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
