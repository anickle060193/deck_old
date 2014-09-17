package com.adamnickle.deck;


import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.Player;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiView;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.util.HashMap;

public class GameFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    private static final boolean FREE_SEND_MODE = true;

    private int mLastOrientation;
    private GameView mGameView;
    private GameUiView mGameUiView;
    private GameConnection mGameConnection;

    /* Game specific fields */
    private Player mLocalPlayer;
    private HashMap<String, Player > mPlayers;
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
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        super.onPrepareOptionsMenu( menu );

        if( mGameConnection.isServer() )
        {
            menu.findItem( R.id.actionRequestCardFromPlayer ).setVisible( true );
            menu.setGroupVisible( R.id.dealerActions, true );
        }
        else
        {
            menu.findItem( R.id.actionRequestCardFromPlayer ).setVisible( false );
            menu.setGroupVisible( R.id.dealerActions, mIsDealer );
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
                if( mIsDealer || mGameConnection.isServer() )
                {
                    if( mDeck.getCardCount() < mPlayers.size() )
                    {
                        mGameUiView.showPopup( "No Cards Left", "There are not enough cards left to evenly deal to players." );
                    }
                    else
                    {
                        final int maxCardsPerPlayer = mDeck.getCardCount() / mPlayers.size();
                        final Integer[] nums = new Integer[ maxCardsPerPlayer ];
                        for( int i = 1; i <= maxCardsPerPlayer; i++ )
                        {
                            nums[ i - 1 ] = i;
                        }
                        mGameUiView.createSelectItemDialog( "Number of cards to deal to each player:", nums, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface dialogInterface, int index )
                            {
                                int cardsPerPlayer = nums[ index ];
                                for( int i = 0; i < cardsPerPlayer && mDeck.getCardCount() > 0; i++ )
                                {
                                    for( Player player : mPlayers.values() )
                                    {
                                        mGameConnection.sendCard( mGameConnection.getLocalPlayerID(), player.getID(), mDeck.removeTopCard() );
                                    }
                                }
                            }
                        } ).show();
                    }
                }
                return true;
            }

            case R.id.actionClearPlayerHands:
            {
                if( mIsDealer || mGameConnection.isServer() )
                {
                    for( Player player : mPlayers.values() )
                    {
                        mGameConnection.clearPlayerHand( mLocalPlayer.getID(), player.getID() );
                    }
                }
                mDeck.resetCards();
                return true;
            }

            case R.id.actionDealSingleCard:
            {
                if( mIsDealer || mGameConnection.isServer() )
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
                                mGameConnection.sendCard( mGameConnection.getLocalPlayerID(), playerID, mDeck.removeTopCard() );
                                dialogInterface.dismiss();
                            }
                        } ).show();
                    }
                }
                return true;
            }

            case R.id.actionRequestCardFromPlayer:
            {
                if( mPlayers.size() == 0 )
                {
                    mGameUiView.showPopup( "No Players Connected", "There are not players connected to the current game to select from." );
                } else
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
                            mGameConnection.requestCard( mGameConnection.getLocalPlayerID(), playerID );
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
        if( FREE_SEND_MODE || canSendCard() )
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
                            mGameUiView.removeCardDrawable( card );
                            mGameConnection.sendCard( mLocalPlayer.getID(), player.getID(), card );
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
        return FREE_SEND_MODE || ( mCanSendCard > 0 );
    }

    @Override
    public void setGameUiInterface( GameUiView gameUiView )
    {
        mGameUiView = gameUiView;
        for( Card card : mLocalPlayer.getCards() )
        {
            mGameUiView.addCardDrawable( card );
        }
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public void setGameConnection( GameConnection gameConnection )
    {
        mGameConnection = gameConnection;
        mLocalPlayer = new Player( mGameConnection.getLocalPlayerID(), mGameConnection.getDefaultLocalPlayerName() );
        mPlayers.put( mLocalPlayer.getID(), mLocalPlayer );
    }

    @Override
    public void onPlayerConnect( Player newPlayer )
    {
        mPlayers.put( newPlayer.getID(), newPlayer );
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( newPlayer.getName() + " joined the game." );
        }
    }

    @Override
    public void onPlayerDisconnect( String playerID )
    {
        Player player = mPlayers.remove( playerID );
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( player.getName() + " left game." );
        }
    }

    @Override
    public void onServerConnect( String serverID, String serverName )
    {
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( "Connected to " + serverName + "'s server" );
        }
    }

    @Override
    public void onServerDisconnect( String serverID )
    {
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( "Server closed." );
        }
        getActivity().finish();
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
    public void onConnectionStateChange( int newState )
    {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCardReceive( String senderID, String receiverID, Card card )
    {
        mLocalPlayer.addCard( card );
        if( mGameUiView != null )
        {
            mGameUiView.addCardDrawable( card );
        }
    }

    @Override
    public void onCardRequested( String requesterID, String requesteeID )
    {
        mCanSendCard++;
        if( mGameUiView != null )
        {
            mGameUiView.displayNotification( mPlayers.get( requesterID ).getName() + " requested a card" );
        }
    }

    @Override
    public void onClearPlayerHand( String commanderID, String commandeeID )
    {
        mLocalPlayer.clearHand();
        if( mGameUiView != null )
        {
            mGameUiView.removeAllCardDrawables();
            mGameUiView.displayNotification( mPlayers.get( commanderID ).getName() + " cleared your hand" );
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
        getActivity().invalidateOptionsMenu();
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
