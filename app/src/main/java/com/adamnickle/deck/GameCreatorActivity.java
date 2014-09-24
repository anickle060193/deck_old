package com.adamnickle.deck;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class GameCreatorActivity extends Activity
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
