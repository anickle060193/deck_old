package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;


public class GameActivity extends Activity
{
    private static final String TAG = GameActivity.class.getSimpleName();

    private ConnectionInterfaceFragment mGameConnection;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final int connectionType = intent.getIntExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_TYPE, ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT );
            final String className = intent.getStringExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_CLASS_NAME );
            try
            {
                mGameConnection = (ConnectionInterfaceFragment)Class.forName( className ).newInstance();
                getFragmentManager()
                        .beginTransaction()
                        .add( mGameConnection, mGameConnection.getClass().getName() )
                        .commit();

                final GameFragment gameFragment = new GameFragment();
                gameFragment.setConnectionInterface( mGameConnection );
                gameFragment.setConnectionType( connectionType );
                mGameConnection.setConnectionListener( gameFragment );

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
        switch( mGameConnection.getConnectionType() )
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
