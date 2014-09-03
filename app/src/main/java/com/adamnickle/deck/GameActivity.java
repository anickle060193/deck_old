package com.adamnickle.deck;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.spi.ConnectionInterface;


public class GameActivity extends Activity
{
    private static final String TAG = GameActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {
            getFragmentManager()
                    .beginTransaction()
                    .replace( android.R.id.content, new GameStartFragment(), GameStartFragment.FRAGMENT_NAME )
                    .commit();
        }
    }

    private static class GameStartFragment extends Fragment
    {
        public static final String FRAGMENT_NAME = GameStartFragment.class.getSimpleName();

        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            return inflater.inflate( R.layout.game_start_fragment, container, false );
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
            super.onActivityCreated( savedInstanceState );
            getActivity().findViewById( R.id.createGame ).setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    createGameFragment( ConnectionInterface.CONNECTION_TYPE_SERVER );
                }
            } );

            getActivity().findViewById( R.id.joinGame ).setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    createGameFragment( ConnectionInterface.CONNECTION_TYPE_CLIENT );
                }
            } );
        }

        private void createGameFragment( int connectionType )
        {
            final BluetoothConnectionFragment bluetoothConnectionFragment = new BluetoothConnectionFragment();
            final GameFragment gameFragment = new GameFragment( connectionType, bluetoothConnectionFragment );

            bluetoothConnectionFragment.setConnectionListener( gameFragment );
            bluetoothConnectionFragment.startConnection( connectionType );

            getActivity().getFragmentManager()
                    .beginTransaction()
                    .add( bluetoothConnectionFragment, BluetoothConnectionFragment.FRAGMENT_NAME )
                    .replace( android.R.id.content, gameFragment, GameFragment.FRAGMENT_NAME )
                    .commit();
        }
    }
}
