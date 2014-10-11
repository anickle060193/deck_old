package com.adamnickle.deck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.crashlytics.android.Crashlytics;


public class MainActivity extends Activity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        Crashlytics.start( this );

        Log.d( TAG, "+++ ON CREATE +++" );
        setContentView( R.layout.activity_main );

        findViewById( R.id.startGameButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent startServer = new Intent( MainActivity.this, GameActivity.class );
                startServer.putExtra( ConnectionFragment.EXTRA_CONNECTION_TYPE, ConnectionFragment.ConnectionType.SERVER );
                startServer.putExtra( ConnectionFragment.EXTRA_CONNECTION_CLASS_NAME, BluetoothConnectionFragment.class.getName() );
                startActivity( startServer );
            }
        } );

        findViewById( R.id.joinGameButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent startClient = new Intent( MainActivity.this, GameActivity.class );
                startClient.putExtra( ConnectionFragment.EXTRA_CONNECTION_TYPE, ConnectionFragment.ConnectionType.CLIENT );
                startClient.putExtra( ConnectionFragment.EXTRA_CONNECTION_CLASS_NAME, BluetoothConnectionFragment.class.getName() );
                startActivity( startClient );
            }
        } );

        findViewById( R.id.settingsButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent openSettings = new Intent( MainActivity.this, DeckSettingsActivity.class );
                startActivity( openSettings );
            }
        } );
    }
}
