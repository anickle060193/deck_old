package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder( this )
                .setTitle( "Close Game" )
                .setMessage( "Cancel game creation?" )
                .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        GameCreatorActivity.this.finish();
                    }
                } )
                .setNegativeButton( "Cancel", null )
                .show();
    }
}
