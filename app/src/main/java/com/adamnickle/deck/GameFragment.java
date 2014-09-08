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
import android.view.WindowManager;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.ClientGame;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.ServerGame;
import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.ConnectionListener;
import com.adamnickle.deck.Interfaces.GameConnectionInterface;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiInterface;

public class GameFragment extends Fragment implements ConnectionListener, GameConnectionInterface
{
    public static final String FRAGMENT_NAME = GameFragment.class.getSimpleName();

    private int mLastOrientation;
    private Game mGame;
    private GameView mGameView;
    private GameConnectionListener mGameConnectionListener;
    private ConnectionInterfaceFragment mConnection;

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
        getActivity().getActionBar().hide();
        getActivity().getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

        if( mGameView == null )
        {
            mGameView = new GameView( getActivity() );
            mGameView.setGameUiListener( mGame );

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
        mGame.setGameUiInterface( mGameView );

        return mGameView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        switch( mConnection.getConnectionType() )
        {
            case ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT:
                if( mConnection.getState() == ConnectionInterfaceFragment.STATE_NONE )
                {
                    mConnection.findServer();
                }
                break;

            case ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER:
                if( mConnection.getState() == ConnectionInterfaceFragment.STATE_NONE )
                {
                    mConnection.startConnection();
                }
                break;
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

        final int connectionType = mConnection.getConnectionType();

        switch( connectionType )
        {
            case ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT:
                menu.findItem( R.id.actionDealCards ).setVisible( false );
                menu.findItem( R.id.actionClearPlayerHands ).setVisible( false );
                menu.findItem( R.id.actionDealSingleCard ).setVisible( false );
                menu.findItem( R.id.actionRequestCardFromPlayer ).setVisible( false );
                break;

            case ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER:
                menu.findItem( R.id.actionDealCards ).setVisible( true );
                menu.findItem( R.id.actionClearPlayerHands ).setVisible( true );
                menu.findItem( R.id.actionDealSingleCard ).setVisible( true );
                menu.findItem( R.id.actionRequestCardFromPlayer ).setVisible( true );
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.actionDealCards:
                return true;

            case R.id.actionClearPlayerHands:
                mGame.clearPlayerHands();
                return true;

            case R.id.actionDealSingleCard:
                return true;

            case R.id.actionRequestCardFromPlayer:
                if( mGame.getPlayerCount() == 0 )
                {
                    new AlertDialog.Builder( getActivity() )
                            .setTitle( "No Players Connected" )
                            .setMessage( "There are not players connected to the current game to select from." )
                            .setPositiveButton( "OK", null )
                            .show();
                }
                else
                {
                    selectItem( "Select player:", mGame.getPlayerNames(), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int index )
                        {
                            //TODO Do something
                            final String key = mGame.getPlayerIDs()[ index ];
                            dialogInterface.dismiss();
                        }
                    } );
                }
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public void onDestroy()
    {
        mConnection.stopConnection();

        super.onDestroy();
    }

    public void selectItem( String title, final String items[], DialogInterface.OnClickListener listener )
    {
        new AlertDialog.Builder( getActivity() )
                .setTitle( title )
                .setItems( items, listener )
                .show();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageReceive( String senderID, int bytes, byte[] data )
    {
        int messageType = (int) data[ 0 ];
        switch( messageType )
        {
            case GameMessageParser.MESSAGE_CARD:
                final Card card = GameMessageParser.parseCardMessage( data );
                mGameConnectionListener.onCardReceive( senderID, card );
                break;

            case GameMessageParser.MESSAGE_CARD_SEND_REQUEST:
                mGameConnectionListener.onCardSendRequested( senderID ); //TODO Add something about valid cards
                break;

            case GameMessageParser.MESSAGE_PLAYER_NAME:
                final String name = GameMessageParser.parsePlayerNameMessage( data );
                mGameConnectionListener.onReceivePlayerName( senderID, name );
                break;

            case GameMessageParser.MESSAGE_CLEAR_HAND:
                mGameConnectionListener.onClearPlayerHand( senderID );
                break;
        }
    }

    @Override
    public void onDeviceConnect( String deviceID, String deviceName )
    {
        mGameConnectionListener.onPlayerConnect( deviceID, deviceName );
    }

    @Override
    public void onNotification( String notification )
    {
        mGameConnectionListener.onNotification( notification );
    }

    @Override
    public void onConnectionStateChange( int newState )
    {
        mGameConnectionListener.onConnectionStateChange( newState );
    }

    @Override
    public void onConnectionLost( String deviceID )
    {
        mGameConnectionListener.onPlayerDisconnect( deviceID );
        if( mConnection.getConnectionType() == ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT )
        {
            getActivity().finish();
        }
    }

    @Override
    public void onConnectionFailed()
    {
        mGameConnectionListener.onNotification( "Connection failed." );
        if( mConnection.getConnectionType() == ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT )
        {
            mConnection.findServer();
        }
    }

    /*******************************************************************
     * GameConnectionInterface Methods
     *******************************************************************/
    @Override
    public void setConnectionType( int connectionType )
    {
        if( connectionType != mConnection.getConnectionType() )
        {
            if( connectionType == ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT )
            {
                mGame = new ClientGame( getActivity(), this );
                mConnection.setConnectionType( ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT );
            }
            else if( connectionType == ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER )
            {
                mGame = new ServerGame( getActivity(), this );
                mConnection.setConnectionType( ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER );
            }

            mGame.setGameUiInterface( (GameUiInterface) getView() );
            mGameConnectionListener = mGame;
        }
    }

    @Override
    public void setConnectionInterface( ConnectionInterfaceFragment connectionInterfaceFragment )
    {
        mConnection = connectionInterfaceFragment;
    }

    @Override
    public void sendCard( String toDeviceID, Card card )
    {
        mConnection.sendDataToDevice( toDeviceID, GameMessageParser.createCardMessage( card ) );
    }

    @Override
    public void requestCard( String fromDeviceID )
    {
        mConnection.sendDataToDevice( fromDeviceID, GameMessageParser.createCardRequestMessage() );
    }

    @Override
    public void clearPlayerHand( String deviceID )
    {
        mConnection.sendDataToDevice( deviceID, GameMessageParser.createClearHandMessage() );
    }

    @Override
    public String getDefaultLocalPlayerID()
    {
        return mConnection.getDefaultLocalDeviceID();
    }

    @Override
    public String getDefaultLocalPlayerName()
    {
        return mConnection.getDefaultLocalDeviceName();
    }
}
