package com.adamnickle.deck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.adamnickle.deck.spi.ConnectionInterfaceFragment;


public class GameActivity extends Activity
{
    private static final String TAG = GameActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final int connectionType = intent.getIntExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_TYPE, ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT );
            final String className = intent.getStringExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_CLASS_NAME );
            final ConnectionInterfaceFragment connectionInterfaceFragment;
            try
            {
                    connectionInterfaceFragment = (ConnectionInterfaceFragment)Class.forName( className ).newInstance();
                    getFragmentManager()
                            .beginTransaction()
                            .add( connectionInterfaceFragment, connectionInterfaceFragment.getClass().getName() )
                            .commit();

                    final GameFragment gameFragment = new GameFragment();
                    gameFragment.setConnectionInterface( connectionInterfaceFragment );
                    gameFragment.setConnectionType( connectionType );
                    connectionInterfaceFragment.setConnectionListener( gameFragment );

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
}
