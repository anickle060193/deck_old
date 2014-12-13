package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.adamnickle.deck.Game.DeckSettings;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Interfaces.ConnectionFragment;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.gmariotti.changelibs.library.view.ChangeLogListView;
import ru.noties.debug.Debug;


public class MainActivity extends ActionBarActivity
{
    public static final int REQUEST_CREATE_GAME = 1;

    private Crouton mCrouton;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Debug.d();
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        findViewById( R.id.quickStartGameButton ).setOnClickListener( new View.OnClickListener()
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

        findViewById( R.id.newGameButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent createGame = new Intent( MainActivity.this, GameCreatorWizardActivity.class );
                startActivityForResult( createGame, REQUEST_CREATE_GAME );
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

        findViewById( R.id.infoButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                Intent openInfo = new Intent( MainActivity.this, InfoActivity.class );
                startActivity( openInfo );
            }
        } );

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        int lastShownVersion = sharedPreferences.getInt( DeckSettings.CHANGE_LOG_SHOWN_VERSION, 0 );
        if( lastShownVersion != BuildConfig.VERSION_CODE )
        {
            new AlertDialog.Builder( this )
                    .setTitle( "Deck Change Log" )
                    .setView( new ChangeLogListView( this ) )
                    .setPositiveButton( "Close", null )
                    .show();

            sharedPreferences.edit()
                    .putInt( DeckSettings.CHANGE_LOG_SHOWN_VERSION, BuildConfig.VERSION_CODE )
                    .apply();
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == REQUEST_CREATE_GAME )
        {
            switch( resultCode )
            {
                case Activity.RESULT_CANCELED:
                    mCrouton = Crouton.makeText( this, "Game creation canceled", Style.INFO );
                    break;

                case Activity.RESULT_OK:
                    final Game game = data.getParcelableExtra( GameCreatorActivity.EXTRA_GAME );

                    final Intent startServer = new Intent( MainActivity.this, GameActivity.class );
                    startServer.putExtra( ConnectionFragment.EXTRA_CONNECTION_TYPE, ConnectionFragment.ConnectionType.SERVER );
                    startServer.putExtra( ConnectionFragment.EXTRA_CONNECTION_CLASS_NAME, BluetoothConnectionFragment.class.getName() );
                    startServer.putExtra( GameCreatorActivity.EXTRA_GAME, game );
                    startActivityForResult( startServer, GameActivity.REQUEST_START_GAME );
                    break;
            }
        }

        if( data != null )
        {
            final String action = data.getAction();
            if( GameActivity.class.getName().equals( action ) )
            {
                switch( resultCode )
                {
                    case GameActivity.RESULT_DISCONNECTED_FROM_SERVER:
                        mCrouton = Crouton.makeText( this, "Disconnected from server", Style.ALERT );
                        break;

                    case Activity.RESULT_CANCELED:
                        mCrouton = Crouton.makeText( this, "Game Closed", Style.INFO );
                        break;

                    case GameActivity.RESULT_INVALID_VERSIONS:
                        mCrouton = Crouton.makeText( this, "Cannot connect to Server. Deck versions differ.", Style.ALERT );
                        break;
                }
            }
            else if( ConnectionFragment.class.getName().equals( action ) )
            {
                switch( resultCode )
                {
                    case ConnectionFragment.RESULT_BLUETOOTH_NOT_SUPPORTED:
                        mCrouton = Crouton.makeText( this, "Bluetooth not supported by device. Bluetooth must be enabled to use application.", Style.ALERT );
                        break;

                    case ConnectionFragment.RESULT_BLUETOOTH_NOT_ENABLED:
                        mCrouton = Crouton.makeText( this, "Bluetooth was not enabled. Bluetooth must be enabled to use application.", Style.ALERT );
                        break;

                    case ConnectionFragment.RESULT_BLUETOOTH_DISABLED:
                        mCrouton = Crouton.makeText( this, "Bluetooth was disabled", Style.ALERT );
                        break;

                    case ConnectionFragment.RESULT_NOT_CONNECTED_TO_DEVICE:
                        mCrouton = Crouton.makeText( this, "No server device selected.", Style.INFO );
                        break;

                    case ConnectionFragment.RESULT_SERVER_CLOSED:
                        mCrouton = Crouton.makeText( this, "Game Closed", Style.INFO );
                        break;
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged( boolean hasFocus )
    {
        super.onWindowFocusChanged( hasFocus );

        if( hasFocus && mCrouton != null )
        {
            mCrouton.show();
            mCrouton = null;
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
