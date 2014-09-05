package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.adamnickle.deck.spi.ConnectionInterfaceFragment;


public class MainActivity extends ActionBarActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.d( TAG, "+++ ON CREATE +++" );
        setContentView( R.layout.activity_main );

        findViewById( R.id.startGameButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent startServer = new Intent( MainActivity.this, GameActivity.class );
                startServer.putExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_TYPE, ConnectionInterfaceFragment.CONNECTION_TYPE_SERVER );
                startServer.putExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_CLASS_NAME, BluetoothConnectionFragment.class.getName() );
                startActivity( startServer );
            }
        } );

        findViewById( R.id.joinGameButton ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent startClient = new Intent( MainActivity.this, GameActivity.class );
                startClient.putExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_TYPE, ConnectionInterfaceFragment.CONNECTION_TYPE_CLIENT );
                startClient.putExtra( ConnectionInterfaceFragment.EXTRA_CONNECTION_CLASS_NAME, BluetoothConnectionFragment.class.getName() );
                startActivity( startClient );
            }
        } );
    }
}
