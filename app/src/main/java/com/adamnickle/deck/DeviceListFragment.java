package com.adamnickle.deck;

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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class DeviceListFragment extends Fragment
{
    private static final String TAG = DeviceListFragment.class.getSimpleName();

    private View mView;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private DeviceListFragmentListener mListener;

    @SuppressWarnings( "ValidFragment" )
    public DeviceListFragment( DeviceListFragmentListener listener )
    {
        mListener = listener;
    }

    public DeviceListFragment() { }

    public void setDeviceListFragmentListener( DeviceListFragmentListener listener )
    {
        mListener = listener;
    }

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
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        getActivity().getActionBar().show();
        getActivity().getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, 0 );

        if( mView == null )
        {
            mView = inflater.inflate( R.layout.activity_device_list, container, false );

            Button scanButton = (Button) mView.findViewById( R.id.button_scan );
            scanButton.setOnClickListener( new View.OnClickListener()
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

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

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
                mPairedDevicesArrayAdapter.add( "No paired devices." );
            }

        }
        return mView;
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

            mListener.onDeviceSelected( info.substring( info.length() - 17 ) );
            getFragmentManager().popBackStackImmediate();
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
                    String noDevices = "No devices found.";
                    mNewDevicesArrayAdapter.add( noDevices );
                }
            }
            else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
            {
                final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR );
                if( state == BluetoothAdapter.STATE_OFF )
                {
                    Toast.makeText( getActivity(), "Bluetooth has been disabled.", Toast.LENGTH_SHORT ).show();
                    getActivity().finish();
                }
            }
        }
    };

    public interface DeviceListFragmentListener
    {
        public void onDeviceSelected( String address );
    }
}
