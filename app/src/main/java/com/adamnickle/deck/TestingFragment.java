package com.adamnickle.deck;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Game;


public class TestingFragment extends Fragment
{
    public static final String FRAGMENT_NAME = TestingFragment.class.getSimpleName();

    private Game mGame;

    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.d( FRAGMENT_NAME, "onCreate" );
        setRetainInstance( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        Log.d( FRAGMENT_NAME, "onCreateView" );
        return inflater.inflate( R.layout.activity_main, container, false );
    }

    @Override
    public void onResume()
    {
        Log.d( FRAGMENT_NAME, "onResume" );
        super.onResume();
        mGame = new Game( 4 );
        mGame.setupGame();
        mGame.playGame();
    }
}
