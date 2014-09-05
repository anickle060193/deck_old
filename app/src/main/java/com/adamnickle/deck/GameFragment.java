package com.adamnickle.deck;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.ClientGame;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.ServerGame;
import com.adamnickle.deck.spi.ConnectionInterfaceFragment;
import com.adamnickle.deck.spi.ConnectionListener;
import com.adamnickle.deck.spi.GameConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionListener;
import com.adamnickle.deck.spi.GameUiInterface;

public class GameFragment extends Fragment implements ConnectionListener, GameConnectionInterface
{
    public static final String FRAGMENT_NAME = GameFragment.class.getSimpleName();

    public static final int MESSAGE_CARD = 1;
    public static final int MESSAGE_CARD_SEND_REQUEST = 2;

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

        final int connectionType = mConnection.getConnectionType();
        if( connectionType == ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT )
        {
            if( mConnection.getState() == ConnectionInterfaceFragment.STATE_NONE )
            {
                mConnection.findServer();
            }
        }
        else if( connectionType == ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER )
        {
            if( mConnection.getState() == ConnectionInterfaceFragment.STATE_NONE )
            {
                mConnection.startConnection();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        mConnection.stopConnection();
        super.onDestroy();
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageReceive( String senderAddress, int bytes, byte[] data )
    {
        int messageType = (int) data[ 0 ];
        switch( messageType )
        {
            case MESSAGE_CARD:
                final int cardNumber = data[ 1 ];
                mGameConnectionListener.onCardReceive( senderAddress, new Card( cardNumber ) );
                break;

            case MESSAGE_CARD_SEND_REQUEST:
                mGameConnectionListener.onCardSendRequested( senderAddress ); //TODO Add something about valid cards
                break;
        }
    }

    @Override
    public void onDeviceConnect( String deviceAddress, String deviceName )
    {
        mGameConnectionListener.onPlayerConnect( deviceAddress, deviceName );
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
    public void onConnectionLost( String deviceAddress )
    {
        mGameConnectionListener.onPlayerDisconnect( deviceAddress );
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
    public void sendCard( String toDeviceAddress, Card card )
    {
        byte[] data = { MESSAGE_CARD, (byte) card.getCardNumber() };
        mConnection.sendDataToDevice( toDeviceAddress, data );
    }

    @Override
    public void requestCard( String fromDeviceAddress )
    {
        byte[] data = { MESSAGE_CARD_SEND_REQUEST };
        mConnection.sendDataToDevice( fromDeviceAddress, data );
    }
}
