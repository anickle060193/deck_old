package com.adamnickle.deck;

import android.app.Activity;
import android.os.Bundle;


public class GameActivity extends Activity
{
    private static final String TAG = "GameActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {
            getFragmentManager()
                    .beginTransaction()
                    .replace( android.R.id.content, new GameFragment() )
                    .commit();
        }
    }
}
