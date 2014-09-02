package com.adamnickle.deck;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.adamnickle.deck.Game.ClientGame;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.GameConnection;
import com.adamnickle.deck.spi.BluetoothConnectionInterface;
import com.adamnickle.deck.spi.BluetoothConnectionListener;

public class GameFragment extends Fragment
{
    public static final String FRAGMENT_NAME = GameFragment.class.getSimpleName();

    private int mLastOrientation;
    private GameConnection mGameConnection;
    private Game mGame;
    private GameView mGameView;

    public GameFragment()
    {
        mGameConnection = new GameConnection();
        mGame = new ClientGame();

        mGame.setGameConnectionInterface( mGameConnection );
        mGameConnection.setGameConnectionListener( mGame );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    public BluetoothConnectionListener getBluetoothConnectionListener()
    {
        return mGameConnection;
    }

    public void setBluetoothConnectionInterface( BluetoothConnectionInterface bluetoothConnectionInterface)
    {
        mGameConnection.setBluetoothConnectionInterface( bluetoothConnectionInterface );
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
}
