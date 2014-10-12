package com.adamnickle.deck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.crashlytics.android.Crashlytics;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


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
                startActivityForResult( startServer, GameActivity.REQUEST_START_GAME );
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
                startActivityForResult( startClient, GameActivity.REQUEST_START_GAME );
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

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        switch( requestCode )
        {
            case GameActivity.REQUEST_START_GAME:
                switch( resultCode )
                {
                    case GameActivity.RESULT_BLUETOOTH_DISABLED:
                        Crouton.makeText( this, "Bluetooth was disabled", Style.ALERT ).show();
                        break;

                    case GameActivity.RESULT_DISCONNECTED_FROM_SERVER:
                        Crouton.makeText( this, "Disconnected from server", Style.ALERT ).show();
                        break;

                    case GameActivity.RESULT_BLUETOOTH_NOT_ENABLED:
                        Crouton.makeText( this, "Bluetooth was not enabled. Bluetooth must be enabled to use application.", Style.ALERT ).show();
                        break;

                    case GameActivity.RESULT_BLUETOOTH_NOT_SUPPORTED:
                        Crouton.makeText( this, "Bluetooth not supported by device. Bluetooth must be enabled to use application.", Style.ALERT ).show();
                        break;

                    case GameActivity.RESULT_NOT_CONNECTED_TO_DEVICE:
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if( isFinishing() )
        {
            Crouton.clearCroutonsForActivity( this );
        }
    }
}
