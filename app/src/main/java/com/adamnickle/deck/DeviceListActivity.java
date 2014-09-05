package com.adamnickle.deck;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class DeviceListActivity extends ActionBarActivity
{
    private static final String TAG = "DeviceListActivity";

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.activity_device_list );

        setResult( Activity.RESULT_CANCELED );

        Button scanButton = (Button) findViewById( R.id.button_scan );
        scanButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                doDiscovery();
                view.setVisibility( View.GONE );
            }
        } );

        mPairedDevicesArrayAdapter = new ArrayAdapter< String >( this, R.layout.device_name );
        mNewDevicesArrayAdapter = new ArrayAdapter< String >( this, R.layout.device_name );

        ListView pairedListView = (ListView) findViewById( R.id.paired_devices );
        pairedListView.setAdapter( mPairedDevicesArrayAdapter );
        pairedListView.setOnItemClickListener( mDeviceClickListener );

        ListView newDevicesListView = (ListView) findViewById( R.id.new_devices );
        newDevicesListView.setAdapter( mNewDevicesArrayAdapter );
        newDevicesListView.setOnItemClickListener( mDeviceClickListener );

        IntentFilter filter = new IntentFilter();
        filter.addAction( BluetoothDevice.ACTION_FOUND );
        filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
        this.registerReceiver( mReceiver, filter );

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if( !pairedDevices.isEmpty() )
        {
            findViewById( R.id.title_paired_devices ).setVisibility( View.VISIBLE );
            for( BluetoothDevice device : pairedDevices )
            {
                mPairedDevicesArrayAdapter.add( device.getName() + "\n" + device.getAddress() );
            }
        }
        else
        {
            mPairedDevicesArrayAdapter.add( "No paired devices." );
        }
    }

    @Override
    protected void onDestroy()
    {
        if( mBluetoothAdapter != null )
        {
            mBluetoothAdapter.cancelDiscovery();
        }

        this.unregisterReceiver( mReceiver );

        super.onDestroy();
    }

    private void doDiscovery()
    {
        Log.d( TAG, "doDiscovery()" );

        setSupportProgressBarIndeterminateVisibility( true );
        setTitle( "Scanning..." );

        findViewById( R.id.title_new_devices ).setVisibility( View.VISIBLE );

        if( mBluetoothAdapter.isDiscovering() )
        {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick( AdapterView< ? > adapterView, View view, int i, long l )
        {
            final String info = (( TextView)view).getText().toString();
            if( info.startsWith( "No" ) )
            {
                return;
            }
            mBluetoothAdapter.cancelDiscovery();

            final String address = info.substring( info.length() - 17 );

            final Intent intent = new Intent();
            intent.putExtra( EXTRA_DEVICE_ADDRESS, address );

            setResult( RESULT_OK, intent );
            finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = intent.getAction();

            if( BluetoothDevice.ACTION_FOUND.equals( action ) )
            {
                BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                if( device.getBondState() != BluetoothDevice.BOND_BONDED )
                {
                    mNewDevicesArrayAdapter.add( device.getName() + "\n" + device.getAddress() );
                }
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
            {
                setSupportProgressBarIndeterminateVisibility( false );
                setTitle( "Select device..." );
                if( mNewDevicesArrayAdapter.getCount() == 0 )
                {
                    String noDevices = "No devices found.";
                    mNewDevicesArrayAdapter.add( noDevices );
                }
            }
            else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
            {
                final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR );
                if( state == BluetoothAdapter.STATE_OFF )
                {
                    Toast.makeText( DeviceListActivity.this, "Bluetooth has been disabled.", Toast.LENGTH_SHORT ).show();
                    finish();
                }
            }
        }
    };
}
