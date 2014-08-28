package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class GameActivity extends ActionBarActivity
{
    private static final String TAG = "GameActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( new GameView( this ) );
    }
}
