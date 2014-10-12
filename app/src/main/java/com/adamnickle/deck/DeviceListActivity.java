package com.adamnickle.deck;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;


public class DeviceListActivity extends Activity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );

        getFragmentManager()
                .beginTransaction()
                .replace( android.R.id.content, new DeviceListFragment() )
                .commit();
    }

    public static class DeviceListFragment extends Fragment
    {
        private static final String TAG = DeviceListFragment.class.getSimpleName();

        private static final int REQUEST_BLUETOOTH_ENABLE = 1;

        private View mView;
        private BluetoothAdapter mBluetoothAdapter;
        private ArrayAdapter< String > mPairedDevicesArrayAdapter;
        private ArrayAdapter< String > mNewDevicesArrayAdapter;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );

            IntentFilter filter = new IntentFilter();
            filter.addAction( BluetoothDevice.ACTION_FOUND );
            filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
            filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
            getActivity().registerReceiver( mReceiver, filter );

            getActivity().setResult( Activity.RESULT_CANCELED );

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if( !mBluetoothAdapter.isEnabled() )
            {
                Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                startActivityForResult( intent, REQUEST_BLUETOOTH_ENABLE );
            }
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            if( mView == null )
            {
                mView = inflater.inflate( R.layout.activity_device_list, container, false );

                mView.findViewById( R.id.button_scan ).setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick( View view )
                    {
                        doDiscovery();
                        view.setVisibility( View.GONE );
                    }
                } );

                mPairedDevicesArrayAdapter = new ArrayAdapter< String >( getActivity(), R.layout.device_name );
                mNewDevicesArrayAdapter = new ArrayAdapter< String >( getActivity(), R.layout.device_name );

                ListView pairedListView = (ListView) mView.findViewById( R.id.paired_devices );
                pairedListView.setAdapter( mPairedDevicesArrayAdapter );
                pairedListView.setOnItemClickListener( mDeviceClickListener );

                ListView newDevicesListView = (ListView) mView.findViewById( R.id.new_devices );
                newDevicesListView.setAdapter( mNewDevicesArrayAdapter );
                newDevicesListView.setOnItemClickListener( mDeviceClickListener );

                mView.findViewById( R.id.title_new_devices ).setVisibility( View.GONE );
                newDevicesListView.setVisibility( View.GONE );

                Set< BluetoothDevice > pairedDevices = mBluetoothAdapter.getBondedDevices();

                if( !pairedDevices.isEmpty() )
                {
                    mView.findViewById( R.id.title_paired_devices ).setVisibility( View.VISIBLE );
                    for( BluetoothDevice device : pairedDevices )
                    {
                        mPairedDevicesArrayAdapter.add( device.getName() + "\n" + device.getAddress() );
                    }
                }
                else
                {
                    mView.findViewById( R.id.title_paired_devices ).setVisibility( View.GONE );
                    mView.findViewById( R.id.paired_devices ).setVisibility( View.GONE );
                }

            }
            return mView;
        }

        @Override
        public void onActivityResult( int requestCode, int resultCode, Intent data )
        {
            switch( requestCode )
            {
                case REQUEST_BLUETOOTH_ENABLE:
                    if( resultCode != Activity.RESULT_OK )
                    {
                        Intent intent = new Intent();
                        intent.putExtra( BluetoothConnectionFragment.EXTRA_NOTIFICATION, "Bluetooth was not enabled" );
                        getActivity().setResult( BluetoothConnectionFragment.RESULT_FIND_DEVICE_FAIL, intent );
                        getActivity().finish();
                    }
            }
        }

        @Override
        public void onDestroy()
        {
            if( mBluetoothAdapter != null )
            {
                mBluetoothAdapter.cancelDiscovery();
            }

            getActivity().unregisterReceiver( mReceiver );

            super.onDestroy();
        }

        private void doDiscovery()
        {
            Log.d( TAG, "doDiscovery()" );

            getActivity().setProgressBarIndeterminateVisibility( true );
            getActivity().setTitle( "Scanning..." );

            mView.findViewById( R.id.title_new_devices ).setVisibility( View.VISIBLE );
            mView.findViewById( R.id.new_devices ).setVisibility( View.VISIBLE );

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
                final String info = ( (TextView) view ).getText().toString();
                if( info.startsWith( "No" ) )
                {
                    return;
                }
                mBluetoothAdapter.cancelDiscovery();

                final String address = info.substring( info.length() - 17 );

                Intent intent = new Intent();
                intent.putExtra( BluetoothConnectionFragment.EXTRA_DEVICE_ADDRESS, address );
                getActivity().setResult( Activity.RESULT_OK, intent );
                getActivity().finish();
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
                    getActivity().setProgressBarIndeterminateVisibility( false );
                    getActivity().setTitle( "Select device..." );
                    if( mNewDevicesArrayAdapter.getCount() == 0 )
                    {
                        mNewDevicesArrayAdapter.add( "No devices found." );
                    }
                }
                else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
                {
                    final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR );
                    if( state == BluetoothAdapter.STATE_OFF )
                    {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra( BluetoothConnectionFragment.EXTRA_NOTIFICATION, "Bluetooth was disabled enabled" );
                        getActivity().setResult( BluetoothConnectionFragment.RESULT_FIND_DEVICE_FAIL, returnIntent );
                        getActivity().finish();
                    }
                }
            }
        };
    }
}
