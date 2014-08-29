package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class GameCreatorActivity extends ActionBarActivity
{
    private static final String TAG = "GameCreatorActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.d( TAG, "+++ ON CREATE +++" );
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {
            getFragmentManager()
                    .beginTransaction()
                    .replace( android.R.id.content, new GameCreatorFragment() )
                    .commit();
        }
    }
}
