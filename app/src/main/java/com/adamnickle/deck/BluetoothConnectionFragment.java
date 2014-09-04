package com.adamnickle.deck;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.ConnectionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothConnectionFragment extends Fragment implements ConnectionInterface
{
    private static final String TAG = BluetoothConnectionFragment.class.getSimpleName();

    public static final String FRAGMENT_NAME = BluetoothConnectionFragment.class.getSimpleName();

    private static final UUID MY_UUID = UUID.fromString( "e40042a0-240b-11e4-8c21-0800200c9a66" );
    private static final String SERVICE_NAME = "Deck Server";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    private static int NEXT_DEVICE_ID = 0;

    private final BluetoothAdapter mBluetoothAdapter;
    private ConnectionListener mListener;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ArrayList< ConnectedThread > mConnectedThreads;
    private int mState;
    private int mConnectionType;
    private int mSavedConnectionType;

    public BluetoothConnectionFragment()
    {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = STATE_NONE;
        this.mConnectionType = CONNECTION_TYPE_NONE;

        mConnectedThreads = new ArrayList< ConnectedThread >();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        if( mBluetoothAdapter == null )
        {
            Toast.makeText( getActivity(), "Bluetooth not supported by device. Application exiting.", Toast.LENGTH_LONG ).show();
            this.getActivity().finish();
        }
    }

    @Override
    public void onDestroy()
    {
        this.stopConnection();
        super.onDestroy();
    }

    @Override
    public void startConnection( int connectionType )
    {
        Log.d( TAG, "startConnection" );

        if( !mBluetoothAdapter.isEnabled() )
        {
            mSavedConnectionType = connectionType;
            Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( enableIntent, REQUEST_ENABLE_BLUETOOTH );
        } else
        {
            this.start( connectionType );
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
                    startConnection( mSavedConnectionType );
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
                    this.connect( device );
                }
                break;
        }
    }

    @Override
    public void setConnectionListener( ConnectionListener connectionListener )
    {
        mListener = connectionListener;
    }

    @Override
    public synchronized int getConnectionType()
    {
        return mConnectionType;
    }

    private synchronized void setConnectionType( int connectionType )
    {
        mConnectionType = connectionType;
    }

    @Override
    public synchronized int getState()
    {
        return mState;
    }

    private synchronized void setState( int state )
    {
        Log.d( TAG, "setState() " + mState + " -> " + state );
        mState = state;

        if( mListener != null )
        {
            mListener.onConnectionStateChange( mState );
        }
    }

    public boolean isConnected()
    {
        return ( mState == STATE_CONNECTED ) || ( mState == STATE_CONNECTED_LISTENING );
    }

    private void ensureDiscoverable()
    {
        Log.d( TAG, "ensureDiscoverable" );

        if( mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE )
        {
            Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
            startActivity( discoverableIntent );
        }
    }

    public synchronized void start( int connectionType )
    {
        Log.d( TAG, "BEGIN start()" );
        setConnectionType( connectionType );

        if( connectionType == CONNECTION_TYPE_SERVER )
        {
            ensureDiscoverable();
        }

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        for( ConnectedThread thread : mConnectedThreads )
        {
            thread.cancel();
        }
        mConnectedThreads.clear();

        switch( getConnectionType() )
        {
            case CONNECTION_TYPE_NONE:
            case CONNECTION_TYPE_CLIENT:
                setState( STATE_NONE );
                break;

            case CONNECTION_TYPE_SERVER:
                setState( STATE_LISTENING );

                if( mAcceptThread == null )
                {
                    mAcceptThread = new AcceptThread();
                    mAcceptThread.start();
                }
                break;
        }
    }

    @Override
    public synchronized void restartConnection()
    {
        Log.d( TAG, "BEGIN restartConnection()" );

        if( getConnectionType() == CONNECTION_TYPE_CLIENT )
        {
            start( CONNECTION_TYPE_CLIENT );
            return;
        }

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if( mConnectedThreads.isEmpty() )
        {
            setState( STATE_LISTENING );
        }
        else
        {
            setState( STATE_CONNECTED_LISTENING );
        }

        if( mAcceptThread == null )
        {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    @Override
    public synchronized void finishConnecting()
    {
        Log.d( TAG, "BEGIN finishedConnecting" );

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if( mAcceptThread != null )
        {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState( STATE_CONNECTED );
    }

    @Override
    public synchronized void stopConnection()
    {
        Log.d( TAG, "BEGIN stopConnection" );
        setConnectionType( CONNECTION_TYPE_NONE );

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        for( ConnectedThread thread : mConnectedThreads )
        {
            if( thread != null )
            {
                thread.cancel();
            }
        }
        mConnectedThreads.clear();

        if( mAcceptThread != null )
        {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState( STATE_NONE );
    }

    @Override
    public void findServer()
    {
        Intent devicesIntent = new Intent( getActivity(), DeviceListActivity.class );
        startActivityForResult( devicesIntent, REQUEST_CONNECT_DEVICE );
    }

    @Override
    public synchronized void connect( Object device )
    {
        Log.d( TAG, "connect to: " + device );

        if( !( device instanceof BluetoothDevice ) )
        {
            return;
        }

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mConnectThread = new ConnectThread( (BluetoothDevice) device );
        mConnectThread.start();

        setConnectionType( CONNECTION_TYPE_CLIENT );
        setState( STATE_CONNECTING );
    }

    public synchronized void connected( BluetoothSocket socket, BluetoothDevice device )
    {
        Log.d( TAG, "connected to: " + device );

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if( getConnectionType() == CONNECTION_TYPE_CLIENT )
        {
            for( ConnectedThread thread : mConnectedThreads )
            {
                if( thread != null )
                {
                    thread.cancel();
                }
            }
            mConnectedThreads.clear();

            if( mAcceptThread != null )
            {
                mAcceptThread.cancel();
                mAcceptThread = null;
            }
        }

        ConnectedThread connectedThread = new ConnectedThread( socket );
        mConnectedThreads.add( connectedThread );
        connectedThread.start();

        if( mListener != null )
        {
            mListener.onDeviceConnect( connectedThread.getID(), device.getName() );
        }

        if( getConnectionType() == CONNECTION_TYPE_CLIENT )
        {
            setState( STATE_CONNECTED );
        }
        else
        {
            setState( STATE_CONNECTED_LISTENING );
        }
    }

    private void write( int deviceID, byte[] out )
    {
        if( isConnected() )
        {
            synchronized( this )
            {
                for( ConnectedThread connectedThread : mConnectedThreads )
                {
                    if( connectedThread.getID() == deviceID )
                    {
                        connectedThread.write( out );
                    }
                }
            }
        }
    }

    private void connectionFailed()
    {
        if( mListener != null )
        {
            mListener.onNotification( "Unable to connect to device." );
        }

        switch( getConnectionType() )
        {
            case CONNECTION_TYPE_CLIENT:
                stopConnection();
                break;
            case CONNECTION_TYPE_SERVER:
                restartConnection();
                break;
        }
    }

    private void connectionLost( ConnectedThread connectedThread )
    {
        if( mListener != null )
        {
            mListener.onConnectionLost( connectedThread.getID() );
        }
        mConnectedThreads.remove( connectedThread );

        switch( getConnectionType() )
        {
            case CONNECTION_TYPE_CLIENT:
                stopConnection();
                break;
            case CONNECTION_TYPE_SERVER:
                restartConnection();
                break;
        }
    }

    @Override
    public void sendDataToDevice( int deviceID, byte[] data )
    {
        if( !isConnected() )
        {
            if( mListener != null )
            {
                mListener.onNotification( "Not connected" );
            }
            return;
        }

        if( data.length > 0 )
        {
            write( deviceID, data );
        }
    }

    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread()
        {
            BluetoothServerSocket temp = null;
            try
            {
                temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord( SERVICE_NAME, MY_UUID );
            } catch( IOException io )
            {
                Log.e( TAG, "Socket listen() failed", io );
            }
            mServerSocket = temp;
        }

        @Override
        public void run()
        {
            Log.d( TAG, "BEGIN AcceptThread" );
            setName( "AcceptThread" );

            BluetoothSocket socket;

            while( mState != STATE_CONNECTED )
            {
                try
                {
                    socket = mServerSocket.accept();
                } catch( IOException io )
                {
                    Log.e( TAG, "Accept Thread accept() failed", io );
                    break;
                }

                if( socket != null )
                {
                    synchronized( BluetoothConnectionFragment.this )
                    {
                        switch( mState )
                        {
                            case STATE_LISTENING:
                            case STATE_CONNECTING:
                            case STATE_CONNECTED_LISTENING:
                                connected( socket, socket.getRemoteDevice() );
                                break;

                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try
                                {
                                    socket.close();
                                } catch( IOException io )
                                {
                                    Log.e( TAG, "Could not close unwanted socket", io );
                                }
                                break;
                        }
                    }
                }
            }
            Log.d( TAG, "END AcceptThread" );
        }

        public void cancel()
        {
            Log.d( TAG, "AcceptThread cancel()" );

            try
            {
                mServerSocket.close();
            } catch( IOException io )
            {
                Log.e( TAG, "close() of server failed", io );
            }
        }
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread( BluetoothDevice device )
        {
            mDevice = device;

            BluetoothSocket temp = null;
            try
            {
                temp = device.createRfcommSocketToServiceRecord( MY_UUID );
            } catch( IOException io )
            {
                Log.e( TAG, "ConnectThread create() failed", io );
            }
            mSocket = temp;
        }

        @Override
        public void run()
        {
            Log.d( TAG, "BEGIN ConnectThread" );
            setName( "ConnectThread" );

            mBluetoothAdapter.cancelDiscovery();

            try
            {
                mSocket.connect();
            } catch( IOException io )
            {
                try
                {
                    mSocket.close();
                } catch( IOException io2 )
                {
                    Log.e( TAG, "unable to close()", io2 );
                }
                connectionFailed();
                return;
            }

            synchronized( BluetoothConnectionFragment.this )
            {
                mConnectThread = null;
            }

            connected( mSocket, mDevice );
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            } catch( IOException io )
            {
                Log.e( TAG, "ConnectThread close() of socket failed", io );
            }
        }
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private final int mID;

        public ConnectedThread( BluetoothSocket socket )
        {
            Log.d( TAG, "BEGIN create ConnectedThread" );
            mSocket = socket;
            synchronized( BluetoothConnectionFragment.this )
            {
                mID = NEXT_DEVICE_ID++;
            }

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch( IOException io )
            {
                Log.e( TAG, "Failed to get input/output streams", io );
            }
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        public int getID()
        {
            return mID;
        }

        public void run()
        {
            Log.d( TAG, "BEGIN ConnectedThread" );

            byte[] buffer = new byte[ 1024 ];
            int bytes;

            while( true )
            {
                try
                {
                    bytes = mInputStream.read( buffer );
                    if( bytes > 0 )
                    {
                        if( mListener != null )
                        {
                            mListener.onMessageReceive( mID, bytes, buffer );
                        }
                    }
                } catch( IOException io )
                {
                    Log.e( TAG, "failed to read, disconnected", io );
                    connectionLost( this );
                    break;
                }
            }
        }

        public void write( byte[] buffer )
        {
            try
            {
                mOutputStream.write( buffer );
            } catch( IOException io )
            {
                Log.e( TAG, "Exception during write", io );
            }
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            } catch( IOException io )
            {
                Log.e( TAG, "close() of connected socket failed", io );
            }
        }
    }
}
