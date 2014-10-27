package com.adamnickle.deck;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

import java.util.ArrayList;

import ru.noties.debug.Debug;


public class DeviceListActivity extends ActionBarActivity
{
    private DeviceListFragment mDeviceListFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.content );

        if( savedInstanceState == null )
        {
            mDeviceListFragment = new DeviceListFragment();
        }
        else
        {
            mDeviceListFragment = (DeviceListFragment) getSupportFragmentManager().findFragmentByTag( DeviceListFragment.class.getName() );
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace( R.id.content, mDeviceListFragment, DeviceListFragment.class.getName() )
                .commit();
    }

    public static class DeviceListFragment extends Fragment
    {
        private static final int REQUEST_BLUETOOTH_ENABLE = 1;

        private View mDevicesView;
        private ListView mDevicesList;
        private ActionProcessButton mScanButton;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothDeviceArrayAdapter mDevicesArrayAdapter;
        private boolean mRegisteredReceiver;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if( !mBluetoothAdapter.isEnabled() )
            {
                Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                startActivityForResult( intent, REQUEST_BLUETOOTH_ENABLE );
            }
        }

        @Override
        public void onAttach( Activity activity )
        {
            super.onAttach( activity );

            activity.setResult( Activity.RESULT_CANCELED );
        }

        @Override
        public void onResume()
        {
            super.onResume();

            IntentFilter filter = new IntentFilter();
            filter.addAction( BluetoothDevice.ACTION_FOUND );
            filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
            filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
            getActivity().registerReceiver( mReceiver, filter );
            mRegisteredReceiver = true;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            if( mDevicesView == null )
            {
                mDevicesView = inflater.inflate( R.layout.activity_device_list, container, false );

                mScanButton = (ActionProcessButton) mDevicesView.findViewById( R.id.button_scan );
                mScanButton.setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick( View view )
                    {
                        mScanButton.setProgress( 1 );
                        mScanButton.setEnabled( false );
                        doDiscovery();
                    }
                } );

                final ArrayList< BluetoothDevice > devices = new ArrayList< BluetoothDevice >( mBluetoothAdapter.getBondedDevices() );
                mDevicesArrayAdapter = new BluetoothDeviceArrayAdapter( getActivity(), devices );

                mDevicesList = (ListView) mDevicesView.findViewById( R.id.devices_list );
                mDevicesList.setAdapter( mDevicesArrayAdapter );
                mDevicesList.setOnItemClickListener( mDeviceClickListener );

                final TextView emptyView = new TextView( getActivity() );
                emptyView.setText( "No devices" );
                mDevicesList.setEmptyView( emptyView );
            }
            else
            {
                container.removeView( mDevicesView );
            }

            return mDevicesView;
        }

        @Override
        public void onActivityResult( int requestCode, int resultCode, Intent data )
        {
            switch( requestCode )
            {
                case REQUEST_BLUETOOTH_ENABLE:
                    if( resultCode == Activity.RESULT_OK )
                    {
                        this.setRetainInstance( false );
                        getActivity().recreate();
                    }
                    else
                    {
                        getActivity().setResult( GameActivity.RESULT_BLUETOOTH_NOT_ENABLED );
                        getActivity().finish();
                    }
            }
        }

        @Override
        public void onPause()
        {
            super.onPause();

            if( mRegisteredReceiver )
            {
                Activity activity = getActivity();
                if( activity != null )
                {
                    activity.unregisterReceiver( mReceiver );
                }
            }
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            if( mBluetoothAdapter != null )
            {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        private void doDiscovery()
        {
            Debug.d( "doDiscovery()" );

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
                mBluetoothAdapter.cancelDiscovery();

                final BluetoothDevice device = (BluetoothDevice) adapterView.getAdapter().getItem( i );

                Intent intent = new Intent();
                intent.putExtra( BluetoothConnectionFragment.EXTRA_DEVICE_ADDRESS, device.getAddress() );
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
                        if( mDevicesArrayAdapter.getPosition( device ) == -1 )
                        {
                            mDevicesArrayAdapter.add( device );
                        }
                    }
                }
                else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
                {
                    mScanButton.setProgress( 0 );
                    mScanButton.setEnabled( true );
                }
                else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
                {
                    final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR );
                    if( state == BluetoothAdapter.STATE_OFF )
                    {
                        getActivity().setResult( GameActivity.RESULT_BLUETOOTH_DISABLED );
                        getActivity().finish();
                    }
                }
            }
        };
    }

    public static class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice>
    {
        public BluetoothDeviceArrayAdapter( Context context, ArrayList< BluetoothDevice > devices )
        {
            super( context, android.R.layout.simple_list_item_1, devices );
        }

        private static class Holder
        {
            TextView DeviceNameTextView;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            final View row;
            final Holder holder;

            if( convertView == null )
            {
                row = LayoutInflater.from( getContext() ).inflate( android.R.layout.simple_list_item_1, parent, false );
                holder = new Holder();
                holder.DeviceNameTextView = (TextView) row.findViewById( android.R.id.text1 );
                row.setTag( holder );
            }
            else
            {
                row = convertView;
                holder = (Holder) row.getTag();
            }

            final BluetoothDevice device = getItem( position );
            final String deviceName = device.getName();
            if( deviceName == null )
            {
                holder.DeviceNameTextView.setText( "(Unnamed Device)" );
            }
            else
            {
                holder.DeviceNameTextView.setText( device.getName() );
            }

            return row;
        }
    }
}
