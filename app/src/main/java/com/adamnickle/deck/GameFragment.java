package com.adamnickle.deck;


import android.app.Activity;
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

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.DeckSettings;
import com.adamnickle.deck.Game.GameSave;
import com.adamnickle.deck.Game.Player;
import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.adamnickle.deck.Interfaces.GameUiView;

import java.util.HashMap;

public class GameFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    private static final boolean FREE_SEND_MODE = true;

    private int mLastOrientation;
    private GameView mGameView;
    private GameUiView mGameUiView;
    private GameConnection mGameConnection;

    private Player mLocalPlayer;
    private HashMap< String, Player > mPlayers;
    private int mCanSendCard;

    private CardCollection mDeck;
    private boolean mIsDealer;

    public GameFragment()
    {
        mPlayers = new HashMap< String, Player >();
        mCanSendCard = 0;
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
        super.onCreateOptionsMenu( menu, inflater );
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
                final Player players[] = mPlayers.values().toArray( new Player[ mPlayers.size() ] );
                mGameUiView.createSelectItemDialog( "Select dealer:", players, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        Player newDealer = players[ i ];
                        mGameConnection.setDealer( mLocalPlayer.getID(), newDealer.getID(), true );
                    }
                } ).show();
                return true;
            }

            case R.id.actionDealCards:
            {
                if( mDeck.getCardCount() < mPlayers.size() )
                {
                    mGameUiView.showPopup( "No Cards Left", "There are not enough cards left to evenly deal to players." );
                }
                else
                {
                    final int maxCardsPerPlayer = mDeck.getCardCount() / mPlayers.size();
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
                            final Card[][] cardsDealt = new Card[ mPlayers.size() ][ cardsPerPlayer ];
                            for( int i = 0; i < cardsPerPlayer && mDeck.getCardCount() > 0; i++ )
                            {
                                for( int j = 0; j < mPlayers.size(); j++ )
                                {
                                    cardsDealt[ j ][ i ] = mDeck.removeTopCard();
                                }
                            }

                            Player[] players = mPlayers.values().toArray( new Player[ mPlayers.size() ] );
                            for( int i = 0; i < cardsDealt.length; i++ )
                            {
                                mGameConnection.sendCards( mLocalPlayer.getID(), players[ i ].getID(), cardsDealt[ i ], false );
                            }
                        }
                    } ).show();
                }
                return true;
            }

            case R.id.actionClearPlayerHands:
            {
                for( Player player : mPlayers.values() )
                {
                    mGameConnection.clearPlayerHand( mLocalPlayer.getID(), player.getID() );
                }
                mDeck.resetCards();
                return true;
            }

            case R.id.actionDealSingleCard:
            {
                if( mPlayers.size() == 0 )
                {
                    mGameUiView.showPopup( "No Players Connected", "There are not players connected to the current game to select from." );
                }
                else if( mDeck.getCardCount() == 0 )
                {
                    mGameUiView.showPopup( "No Cards Left", "There are no cards left to deal." );
                }
                else
                {
                    final Player[] players = mPlayers.values().toArray( new Player[ mPlayers.size() ] );
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
                            mGameConnection.sendCard( mLocalPlayer.getID(), playerID, mDeck.removeTopCard(), false );
                            dialogInterface.dismiss();
                        }
                    } ).show();
                }
                return true;
            }

            case R.id.actionRequestCardFromPlayer:
            {
                if( mPlayers.size() == 0 )
                {
                    mGameUiView.showPopup( "No Players Connected", "There are not players connected to the current game to select from." );
                }
                else
                {
                    final Player[] players = mPlayers.values().toArray( new Player[ mPlayers.size() ] );
                    final String[] playerNames = new String[ players.length ];
                    final String[] playerIDs = new String[ players.length ];
                    for( int i = 0; i < players.length; i++ )
                    {
                        playerNames[ i ] = players[ i ].getName();
                        playerIDs[ i ] = players[ i ].getID();
                    }
                    mGameUiView.createSelectItemDialog( "Select player:", playerNames, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int index )
                        {
                            final String playerID = playerIDs[ index ];
                            mGameConnection.requestCard( mLocalPlayer.getID(), playerID );
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
                                mGameUiView.sortCards( CardCollection.SortingType.SORT_BY_RANK );
                            }
                            else if( i == 1 )
                            {
                                mGameUiView.sortCards( CardCollection.SortingType.SORT_BY_SUIT );
                            }
                            mGameUiView.layoutCards();
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
                        mGameUiView.createSelectItemDialog( "Select game save:", gameSaves, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface dialogInterface, int i )
                            {
                                if( mGameConnection.openGameSave( getActivity().getApplicationContext(), gameSaves[ i ] ) )
                                {
                                    mGameUiView.displayNotification( "Game open successful." );
                                }
                                else
                                {
                                    mGameUiView.displayNotification( "Game open not successful." );
                                }
                                dialogInterface.dismiss();
                            }
                        } ).show();
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
    public boolean onAttemptSendCard( final Card card )
    {
        if( canSendCard() )
        {
            if( mLocalPlayer.hasCard( card ) && mPlayers.size() > 1 )
            {
                final Player[] players = mPlayers.values().toArray( new Player[ mPlayers.size() ] );
                mGameUiView.createSelectItemDialog( "Select player to send card to:", players, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        final Player player = players[ i ];
                        if( player != null )
                        {
                            mLocalPlayer.removeCard( card );
                            mGameConnection.sendCard( mLocalPlayer.getID(), player.getID(), card, true );
                        }
                        else
                        {
                            mGameUiView.resetCard( card );
                        }
                    }
                } ).setOnCancelListener( new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel( DialogInterface dialogInterface )
                    {
                        mGameUiView.resetCard( card );
                    }
                } ).show();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canSendCard()
    {
        return ( FREE_SEND_MODE || ( mCanSendCard > 0 ) ) && mPlayers.size() > 1;
    }

    @Override
    public void setGameUiInterface( GameUiView gameUiView )
    {
        mGameUiView = gameUiView;
        if( mLocalPlayer != null )
        {
            mLocalPlayer.setPlayerListener( mGameUiView.getPlayerListener() );
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
    public void onPlayerConnect( Player newPlayer )
    {
        mPlayers.put( newPlayer.getID(), newPlayer );
    }

    @Override
    public void onPlayerNameReceive( String senderID, String newName )
    {
        final Player player = mPlayers.get( senderID );
        player.setName( newName );
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( player.getName() + " joined the game." );
        }
    }

    @Override
    public void onPlayerDisconnect( String playerID )
    {
        Player player = mPlayers.remove( playerID );
        if( player != null && mGameUiView != null )
        {
            mGameUiView.displayNotification( player.getName() + " left game." );
        }
    }

    @Override
    public void onServerConnect( String serverID, String serverName )
    {
        if( mGameUiView != null && !mGameConnection.isServer() )
        {
            mGameUiView.displayNotification( "Connected to " + serverName + "'s server" );
        }

        mLocalPlayer = new Player( mGameConnection.getLocalPlayerID(), "ME" );
        if( mGameUiView != null )
        {
            mLocalPlayer.setPlayerListener( mGameUiView.getPlayerListener() );
        }
        mPlayers.put( mLocalPlayer.getID(), mLocalPlayer );

        String playerName = PreferenceManager
                .getDefaultSharedPreferences( getActivity().getApplicationContext() )
                .getString( DeckSettings.PLAYER_NAME, mGameConnection.getDefaultLocalPlayerName() );

        mGameConnection.sendPlayerName( mLocalPlayer.getID(), GameConnection.MOCK_SERVER_ADDRESS, playerName );
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
    public void onConnectionStateChange( Connection.State newState )
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
        mLocalPlayer.addCard( card );
    }

    @Override
    public void onCardsReceive( String senderID, String receiverID, Card[] cards )
    {
        mLocalPlayer.addCards( cards );
    }

    @Override
    public void onCardRequested( String requesterID, String requesteeID )
    {
        mCanSendCard++;
        if( mGameUiView != null )
        {
            String notification;
            if( requesterID.equals( mLocalPlayer.getID() ) )
            {
                notification = "You requested a card from yourself";
            }
            else
            {
                notification = mPlayers.get( requesterID ).getName() + " requested a card";
            }
            mGameUiView.displayNotification( notification );
        }
    }

    @Override
    public void onClearPlayerHand( String commanderID, String commandeeID )
    {
        mLocalPlayer.clearHand();
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
                notification = mPlayers.get( commanderID ).getName() + " cleared your hand.";
            }
            mGameUiView.displayNotification( notification );
        }
    }

    @Override
    public void onSetDealer( String setterID, String setID, boolean isDealer )
    {
        mIsDealer = isDealer;
        if( mGameUiView != null )
        {
            String notification;
            if( mIsDealer )
            {
                notification = mPlayers.get( setterID ).getName() + " made you dealer.";
            }
            else
            {
                notification = mPlayers.get( setterID ).getName() + " unmade you dealer";
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

    @Override
    public void onReceiverCurrentPlayers( String senderID, String receiverID, Player[] players )
    {
        for( Player player : players )
        {
            mPlayers.put( player.getID(), player );
        }
    }
}