package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.adamnickle.deck.Game.ClientGameConnection;
import com.adamnickle.deck.Game.ServerGameConnection;
import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;


public class GameActivity extends Activity
{
    private ConnectionInterfaceFragment mConnection;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final int connectionType = intent.getIntExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_TYPE, ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT );
            final String className = intent.getStringExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_CLASS_NAME );
            try
            {
                mConnection = (ConnectionInterfaceFragment) Class.forName( className ).newInstance();
                mConnection.setConnectionType( connectionType );

                getFragmentManager()
                        .beginTransaction()
                        .add( mConnection, mConnection.getClass().getName() )
                        .commit();

                final GameFragment gameFragment = new GameFragment();

                GameConnection gameConnection = null;
                switch( connectionType )
                {
                    case ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT:
                        gameConnection = new ClientGameConnection( mConnection, gameFragment );
                        break;

                    case ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER:
                        gameConnection = new ServerGameConnection( mConnection, gameFragment );
                        break;
                }
                mConnection.setConnectionListener( gameConnection );

                gameFragment.setGameConnection( gameConnection );

                getFragmentManager()
                        .beginTransaction()
                        .replace( android.R.id.content, gameFragment )
                        .commit();
            }
            catch( ClassCastException e )
            {
                throw new ClassCastException( className + " does not extend " + ConnectionInterfaceFragment.class.getSimpleName() );
            }
            catch( InstantiationException e )
            {
                e.printStackTrace();
            }
            catch( IllegalAccessException e )
            {
                e.printStackTrace();
            }
            catch( ClassNotFoundException e )
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        String message = null;
        switch( mConnection.getConnectionType() )
        {
            case ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT:
                message = "Close current Game? This will disconnect you from server.";
                break;

            case ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER:
                message = "Close current Game? This will disconnect all players.";
                break;
        }

        new AlertDialog.Builder( this )
                .setTitle( "Close Game" )
                .setMessage( message )
                .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        GameActivity.this.finish();
                    }
                } )
                .setNegativeButton( "Cancel", null )
                .show();
    }
}
