package com.adamnickle.deck;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.BluetoothConnectionListener;


public class BluetoothConnectionFragment extends Fragment
{
    private static final String TAG = BluetoothConnectionFragment.class.getSimpleName();

    public static final String FRAGMENT_NAME = BluetoothConnectionFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnection mBluetoothConnection;

    private int mSavedConnectionType;

    public BluetoothConnectionFragment()
    {
        mBluetoothConnection = new BluetoothConnection();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
        setRetainInstance( true );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if( mBluetoothAdapter == null )
        {
            Toast.makeText( getActivity(), "Bluetooth not supported by device. Application exiting.", Toast.LENGTH_LONG ).show();
            this.getActivity().finish();
        }
    }

    @Override
    public void onDestroy()
    {
        if( mBluetoothConnection != null )
        {
            mBluetoothConnection.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        super.onCreateOptionsMenu( menu, inflater );

        inflater.inflate( R.menu.bluetooth_connection, menu );
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        super.onPrepareOptionsMenu( menu );

        int connectionType = mBluetoothConnection.getConnectionType();
        int state = mBluetoothConnection.getState();

        if( connectionType == BluetoothConnection.CONNECTION_TYPE_NONE )
        {
            menu.findItem( R.id.actionCreateServer ).setVisible( true );
            menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
            menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            menu.findItem( R.id.actionCloseServer ).setVisible( false );
            menu.findItem( R.id.actionFindServer ).setVisible( true );
            menu.findItem( R.id.actionLeaveServer ).setVisible( false );
        } else if( connectionType == BluetoothConnection.CONNECTION_TYPE_CLIENT )
        {
            menu.findItem( R.id.actionCreateServer ).setVisible( false );
            menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
            menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            menu.findItem( R.id.actionCloseServer ).setVisible( false );

            if( state == BluetoothConnection.STATE_CONNECTED )
            {
                menu.findItem( R.id.actionFindServer ).setVisible( false );
                menu.findItem( R.id.actionLeaveServer ).setVisible( true );
            } else
            {
                menu.findItem( R.id.actionFindServer ).setVisible( true );
                menu.findItem( R.id.actionLeaveServer ).setVisible( false );
            }
        } else if( connectionType == BluetoothConnection.CONNECTION_TYPE_SERVER )
        {
            menu.findItem( R.id.actionCreateServer ).setVisible( false );
            menu.findItem( R.id.actionCloseServer ).setVisible( true );
            menu.findItem( R.id.actionFindServer ).setVisible( false );
            menu.findItem( R.id.actionLeaveServer ).setVisible( false );

            if( state == BluetoothConnection.STATE_LISTENING )
            {
                menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
                menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            } else if( state == BluetoothConnection.STATE_CONNECTED_LISTENING )
            {
                menu.findItem( R.id.actionFinishConnecting ).setVisible( true );
                menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            } else if( state == BluetoothConnection.STATE_CONNECTED )
            {
                menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
                menu.findItem( R.id.actionRestartConnecting ).setVisible( true );
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch( item.getItemId() )
        {
            case R.id.actionCreateServer:
                mBluetoothConnection.ensureDiscoverable( this.getActivity() );
                startBluetoothConnection( BluetoothConnection.CONNECTION_TYPE_SERVER );
                return true;

            case R.id.actionFinishConnecting:
                mBluetoothConnection.finishConnecting();
                return true;

            case R.id.actionRestartConnecting:
                mBluetoothConnection.restart();
                return true;

            case R.id.actionCloseServer:
                mBluetoothConnection.stop();
                return true;

            case R.id.actionFindServer:
                Intent devicesIntent = new Intent( this.getActivity(), DeviceListActivity.class );
                startActivityForResult( devicesIntent, REQUEST_CONNECT_DEVICE );
                return true;

            case R.id.actionLeaveServer:
                mBluetoothConnection.stop();
                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private void startBluetoothConnection( int connectionType )
    {
        Log.d( TAG, "startBluetoothConnection" );

        if( !mBluetoothAdapter.isEnabled() )
        {
            mSavedConnectionType = connectionType;
            Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( enableIntent, REQUEST_ENABLE_BLUETOOTH );
        } else
        {
            if( mBluetoothConnection != null )
            {
                mBluetoothConnection.start( connectionType );
            }
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        Log.d( TAG, "onActivityResult" );

        switch( requestCode )
        {
            case REQUEST_ENABLE_BLUETOOTH:
                if( resultCode == Activity.RESULT_OK )
                {
                    startBluetoothConnection( mSavedConnectionType );
                    mSavedConnectionType = -1;
                }
                else
                {
                    Log.d( TAG, "Bluetooth not enabled" );
                    Toast.makeText( getActivity(), "Bluetooth was not enabled. Application exiting.", Toast.LENGTH_LONG ).show();
                    getActivity().finish();
                }
                break;

            case REQUEST_CONNECT_DEVICE:
                if( resultCode == Activity.RESULT_OK )
                {
                    String address = data.getStringExtra( DeviceListActivity.EXTRA_DEVICE_ADDRESS );
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( address );
                    mBluetoothConnection.connect( device );
                }
                break;
        }
    }

    public ConnectionInterface getBluetoothConnectionInterface()
    {
        return mBluetoothConnection;
    }

    public void setBluetoothConnectionListener( BluetoothConnectionListener bluetoothConnectionListener )
    {
        mBluetoothConnection.addBluetoothConnectionListener( bluetoothConnectionListener );
    }
}
