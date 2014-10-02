package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.adamnickle.deck.Game.ClientGameConnection;
import com.adamnickle.deck.Game.ServerGameConnection;
import com.adamnickle.deck.Interfaces.Connection;
import com.adamnickle.deck.Interfaces.GameConnection;

import java.security.InvalidParameterException;


public class GameActivity extends Activity
{
    private Connection mConnection;
    private GameConnection mGameConnection;
    private GameFragment mGameFragment;
    private TableFragment mTableFragment;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.activity_game );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final Connection.ConnectionType connectionType = (Connection.ConnectionType) intent.getSerializableExtra( Connection.EXTRA_CONNECTION_TYPE );
            final String className = intent.getStringExtra( Connection.EXTRA_CONNECTION_CLASS_NAME );
            try
            {
                mConnection = (Connection) Class.forName( className ).newInstance();
                mConnection.setConnectionType( connectionType );

                getFragmentManager()
                        .beginTransaction()
                        .add( mConnection, mConnection.getClass().getName() )
                        .commit();

                mGameFragment = new GameFragment();
                mTableFragment = new TableFragment();

                mGameConnection = null;
                switch( connectionType )
                {
                    case CLIENT:
                        mGameConnection = new ClientGameConnection( mConnection );
                        break;

                    case SERVER:
                        mGameConnection = new ServerGameConnection( mConnection );
                        break;

                    default:
                        throw new InvalidParameterException( "Invalid connection type: " + connectionType );
                }
                mConnection.setConnectionListener( mGameConnection );

                mGameConnection.addGameConnectionListener( mGameFragment );
                mGameConnection.addGameConnectionListener( mTableFragment );

                mGameFragment.setGameConnection( mGameConnection );
                mTableFragment.setGameConnection( mGameConnection );

                getFragmentManager()
                        .beginTransaction()
                        .replace( R.id.top, mTableFragment )
                        .replace( R.id.bottom, mGameFragment )
                        .commit();
            }
            catch( ClassCastException e )
            {
                throw new ClassCastException( className + " does not extend " + Connection.class.getSimpleName() );
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
        if( mConnection != null && mConnection.isConnected() )
        {
            String message = null;
            switch( mConnection.getConnectionType() )
            {
                case CLIENT:
                    message = "Close current Game? This will disconnect you from server.";
                    break;

                case SERVER:
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
        else
        {
            GameActivity.this.finish();
        }
    }
}
