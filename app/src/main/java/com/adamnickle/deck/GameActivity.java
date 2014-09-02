package com.adamnickle.deck;

import android.app.Activity;
import android.os.Bundle;


public class GameActivity extends Activity
{
    private static final String TAG = GameActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {
            final BluetoothConnectionFragment bluetoothConnectionFragment = new BluetoothConnectionFragment();
            final GameFragment gameFragment = new GameFragment();

            gameFragment.setBluetoothConnectionInterface( bluetoothConnectionFragment.getBluetoothConnectionInterface() );
            bluetoothConnectionFragment.setBluetoothConnectionListener( gameFragment.getBluetoothConnectionListener() );

            getFragmentManager()
                    .beginTransaction()
                    .add( bluetoothConnectionFragment, BluetoothConnectionFragment.FRAGMENT_NAME )
                    .replace( android.R.id.content, gameFragment, GameFragment.FRAGMENT_NAME )
                    .commit();
        }
    }
}
