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
import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.ConnectionListener;
import com.adamnickle.deck.spi.GameConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionListener;

public class GameFragment extends Fragment implements ConnectionListener, GameConnectionInterface
{
    public static final String FRAGMENT_NAME = GameFragment.class.getSimpleName();

    public static final int MESSAGE_CARD = 1;
    public static final int MESSAGE_CARD_SEND_REQUEST = 2;

    private int mLastOrientation;
    private Game mGame;
    private GameView mGameView;

    private GameConnectionListener mGameConnectionListener;
    private ConnectionInterface mConnection;

    public GameFragment( int connectionType, ConnectionInterface connectionInterface )
    {
        if( connectionType == ConnectionInterface.CONNECTION_TYPE_CLIENT )
        {
            mGame = new ClientGame( this );
        }
        else
        {
            mGame = new ServerGame( this );
        }
        mConnection = connectionInterface;
        mGameConnectionListener = mGame;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
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

            mGame.setGameUiInterface( mGameView );

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

        return mGameView;
    }

    /*******************************************************************
     * BluetoothConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageReceive( int senderID, int bytes, byte[] data )
    {
        int messageType = (int) data[ 0 ];
        switch( messageType )
        {
            case MESSAGE_CARD:
                final int cardNumber = data[ 1 ];
                mGameConnectionListener.onCardReceive( senderID, new Card( cardNumber ) );
                break;

            case MESSAGE_CARD_SEND_REQUEST:
                mGameConnectionListener.onCardSendRequested( senderID ); //TODO Add something about valid cards
                break;
        }
    }

    @Override
    public void onDeviceConnect( int senderID, String deviceName )
    {
        mGameConnectionListener.onPlayerConnect( senderID, deviceName );
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
    public void onConnectionLost( int deviceID )
    {
        mGameConnectionListener.onPlayerDisconnect( deviceID );
    }

    /*******************************************************************
     * GameConnectionInterface Methods
     *******************************************************************/
    @Override
    public void sendCard( int toDeviceID, Card card )
    {
        byte[] data = { MESSAGE_CARD, (byte) card.getCardNumber() };
        mConnection.sendDataToDevice( toDeviceID, data );
    }

    @Override
    public void requestCard( int fromDeviceID )
    {
        byte[] data = { MESSAGE_CARD_SEND_REQUEST };
        mConnection.sendDataToDevice( fromDeviceID, data );
    }
}
