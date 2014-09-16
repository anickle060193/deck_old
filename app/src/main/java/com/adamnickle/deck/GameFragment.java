package com.adamnickle.deck;


import android.app.AlertDialog;
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
import com.adamnickle.deck.Game.Player;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiInterface;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.util.HashMap;

public class GameFragment extends Fragment implements GameConnectionListener, GameUiListener
{
    private int mLastOrientation;
    private GameView mGameView;
    private GameUiInterface mGameUiInterface;
    private GameConnection mGameConnection;

    /* Game specific fields */
    private Player mLocalPlayer;
    private HashMap<String, Player > mPlayers;
    private int mCanSendCard;
    private boolean mIsDealer;

    public GameFragment()
    {
        mPlayers = new HashMap< String, Player >();
        mCanSendCard = 0;
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
            menu.findItem( R.id.actionDealCards ).setVisible( true );
            menu.findItem( R.id.actionClearPlayerHands ).setVisible( true );
            menu.findItem( R.id.actionDealSingleCard ).setVisible( true );
            menu.findItem( R.id.actionRequestCardFromPlayer ).setVisible( true );
        }
        else
        {
            menu.findItem( R.id.actionDealCards ).setVisible( false );
            menu.findItem( R.id.actionClearPlayerHands ).setVisible( false );
            menu.findItem( R.id.actionDealSingleCard ).setVisible( false );
            menu.findItem( R.id.actionRequestCardFromPlayer ).setVisible( false );
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
                selectItem( "Select dealer:", players, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        Player newDealer = players[ i ];
                        mGameConnection.setDealer( mLocalPlayer.getID(), newDealer.getID(), true );
                    }
                } );
                return true;
            }

            case R.id.actionDealCards:
            {
                if( mIsDealer )
                {
                    //TODO Deal cards
                }
                return true;
            }

            case R.id.actionClearPlayerHands:
            {
                if( mIsDealer )
                {
                    for( Player player : mPlayers.values() )
                    {
                        mGameConnection.clearPlayerHand( mLocalPlayer.getID(), player.getID() );
                    }
                }
                return true;
            }

            case R.id.actionDealSingleCard:
            {
                if( mIsDealer )
                {
                    //TODO Deal card
                }
                return true;
            }

            case R.id.actionRequestCardFromPlayer:
            {
                if( mPlayers.size() == 0 )
                {
                    new AlertDialog.Builder( getActivity() )
                            .setTitle( "No Players Connected" )
                            .setMessage( "There are not players connected to the current game to select from." )
                            .setPositiveButton( "OK", null )
                            .show();
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
                    selectItem( "Select player:", playerNames, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int index )
                        {
                            //TODO Do something
                            final String playerID = playerIDs[ index ];
                            dialogInterface.dismiss();
                        }
                    } );
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    public void selectItem( String title, Object items[], DialogInterface.OnClickListener listener )
    {
        String[] itemNames;
        if( items instanceof String[] )
        {
            itemNames = (String[]) items;
        }
        else
        {
            itemNames = new String[ items.length ];
            for( int i = 0; i < items.length; i++ )
            {
                itemNames[ i ] = items[ i ].toString();
            }
        }
        new AlertDialog.Builder( getActivity() )
                .setTitle( title )
                .setItems( itemNames, listener )
                .show();
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( Card card )
    {
        if( mCanSendCard > 0 )
        {
            if( mLocalPlayer.hasCard( card ) )
            {
                mLocalPlayer.removeCard( card );
                mGameConnection.sendCard( mLocalPlayer.getID(), GameConnection.MOCK_SERVER_ADDRESS, card ); //TODO Send to correct person
                return true;
            }
        }
        return false;
    }

    @Override
    public void setGameUiInterface( GameUiInterface gameUiInterface )
    {
        mGameUiInterface = gameUiInterface;
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
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( newPlayer.getName() + " joined the game." );
        }
    }

    @Override
    public void onPlayerDisconnect( String playerID )
    {
        Player player = mPlayers.remove( playerID );
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( player.getName() + " left game." );
        }
    }

    @Override
    public void onServerConnect( String serverID, String serverName )
    {
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( "Connected to " + serverName + "'s server" );
        }
    }

    @Override
    public void onServerDisconnect( String serverID )
    {
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( "Server closed." );
        }
        getActivity().finish();
    }

    @Override
    public void onNotification( String notification )
    {
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( notification );
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
        if( mGameUiInterface != null )
        {
            mGameUiInterface.addCardDrawable( card );
        }
    }

    @Override
    public void onCardRequested( String requesterID, String requesteeID )
    {
        mCanSendCard++;
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( mPlayers.get( requesterID ).getName() + " requested a card" );
        }
    }

    @Override
    public void onClearPlayerHand( String commanderID, String commandeeID )
    {
        mLocalPlayer.clearHand();
        if( mGameUiInterface != null )
        {
            mGameUiInterface.displayNotification( mPlayers.get( commanderID ).getName() + " cleared your hand" );
        }
    }

    @Override
    public void onSetDealer( String setterID, String setID, boolean isDealer )
    {
        mIsDealer = isDealer;
        if( mGameUiInterface != null )
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
            mGameUiInterface.displayNotification( notification );
        }
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
