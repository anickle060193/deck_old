package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.d( TAG, "+++ ON CREATE +++" );
        setContentView( R.layout.activity_main );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.main, menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.actionSettings:
                startActivity( new Intent( this, GameCreatorActivity.class ) );
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    public void onClick( View view )
    {
        startActivity( new Intent( this, GameActivity.class ) );
    }
}
