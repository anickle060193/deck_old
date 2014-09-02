package com.adamnickle.deck;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.BluetoothConnectionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothConnection implements ConnectionInterface
{
    private static final String TAG = BluetoothConnection.class.getSimpleName();

    private static final UUID MY_UUID = UUID.fromString( "e40042a0-240b-11e4-8c21-0800200c9a66" );
    private static final String SERVICE_NAME = "Deck Server";

    public static final int CONNECTION_TYPE_NONE = 0;
    public static final int CONNECTION_TYPE_SERVER = 1;
    public static final int CONNECTION_TYPE_CLIENT = 2;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTED_LISTENING = 4;

    public static final int LOCAL_DEVICE_ID = -1;
    private static int NEXT_DEVICE_ID = 0;

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectionListener mListener;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ArrayList< ConnectedThread > mConnectedThreads;
    private int mState;
    private int mConnectionType;

    public BluetoothConnection()
    {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = STATE_NONE;
        this.mConnectionType = CONNECTION_TYPE_NONE;

        mConnectedThreads = new ArrayList< ConnectedThread >();
    }

    public void addBluetoothConnectionListener( BluetoothConnectionListener bluetoothConnectionListener )
    {
        mListener = bluetoothConnectionListener;
    }

    public synchronized int getConnectionType()
    {
        return mConnectionType;
    }

    private synchronized void setConnectionType( int connectionType )
    {
        mConnectionType = connectionType;
    }

    public synchronized int getState()
    {
        return mState;
    }

    private synchronized void setState( int state )
    {
        Log.d( TAG, "setState() " + mState + " -> " + state );
        mState = state;

        mListener.onConnectionStateChange( mState );
    }

    public boolean isConnected()
    {
        return ( mState == STATE_CONNECTED ) || ( mState == STATE_CONNECTED_LISTENING );
    }

    public void ensureDiscoverable( Context context )
    {
        Log.d( TAG, "ensureDiscoverable" );

        if( mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE )
        {
            Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
            context.startActivity( discoverableIntent );
        }
    }

    public synchronized void start( int connectionType )
    {
        Log.d( TAG, "BEGIN start()" );
        setConnectionType( connectionType );

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

    public synchronized void restart()
    {
        Log.d( TAG, "BEGIN restart()" );

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

    public synchronized void stop()
    {
        Log.d( TAG, "BEGIN stop" );
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

    public synchronized void connect( BluetoothDevice device )
    {
        Log.d( TAG, "connect to: " + device );

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mConnectThread = new ConnectThread( device );
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

        mListener.onDeviceConnect( connectedThread.getID(), device.getName() );

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
        mListener.onNotification( "Unable to connect to device." );
        restart();
    }

    private void connectionLost( ConnectedThread connectedThread )
    {
        mListener.onConnectionLost( connectedThread.getID() );
        mConnectedThreads.remove( connectedThread );

        restart();
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
                    synchronized( BluetoothConnection.this )
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

            synchronized( BluetoothConnection.this )
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
            synchronized( BluetoothConnection.this )
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
                        mListener.onMessageReceive( mID, bytes, buffer );
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

    @Override
    public void sendDataToDevice( int deviceID, byte[] data )
    {
        if( !isConnected() )
        {
            mListener.onNotification( "Not connected" );
            return;
        }

        if( data.length > 0 )
        {
            write( deviceID, data );
        }
    }
}
