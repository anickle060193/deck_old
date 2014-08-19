package com.adamnickle.deck;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Adam on 8/14/2014.
 */
public class BluetoothConnection
{
    private static final String TAG = "BluetoothConnection";
    private static final UUID MY_UUID = UUID.fromString( "e40042a0-240b-11e4-8c21-0800200c9a66" );

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTED_LISTENING = 4;

    public static final int MESSAGE_NEW_DEVICE = 1;
    public static final int MESSAGE_STATE_CHANGED = 2;
    public static final int MESSAGE_TOAST = 3;
    public static final int MESSAGE_WRITE = 4;
    public static final int MESSAGE_READ = 5;

    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ArrayList< ConnectedThread > mConnectedThreads;
    private int mState;
    private boolean mIsClient;

    public BluetoothConnection( Context c, Handler h )
    {
        this.mContext = c;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = h;
        this.mState = STATE_NONE;

        mConnectedThreads = new ArrayList< ConnectedThread >();
    }

    public synchronized boolean getIsClient()
    {
        return mIsClient;
    }

    public synchronized void setIsClient( boolean isClient )
    {
        mIsClient = isClient;
    }

    public synchronized int getState()
    {
        return mState;
    }

    private synchronized void setState( int state )
    {
        Log.d( TAG, "setState() " + mState + " -> " + state );
        mState = state;

        mHandler.obtainMessage( MESSAGE_STATE_CHANGED, mState, -1 ).sendToTarget();
    }

    public void ensureDiscoverable()
    {
        Log.d( TAG, "ensureDiscoverable" );

        if( mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE )
        {
            Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
            mContext.startActivity( discoverableIntent );
        }
    }

    public synchronized void start()
    {
        Log.d( TAG, "BEGIN start()" );

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

        if( mIsClient )
        {
            setState( STATE_NONE );
        }
        else
        {
            setState( STATE_LISTENING );

            if( mAcceptThread == null )
            {
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            }
        }
    }

    public synchronized void restart()
    {
        Log.d( TAG, "BEGIN restart()" );

        if( mIsClient )
        {
            start();
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

        if( mIsClient )
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

        mHandler.obtainMessage( BluetoothConnection.MESSAGE_NEW_DEVICE, device ).sendToTarget();

        if( mIsClient )
        {
            setState( STATE_CONNECTED );
        }
        else
        {
            setState( STATE_CONNECTED_LISTENING );
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

    public void write( byte[] out )
    {
        ConnectedThread connectedThread;
        synchronized( this )
        {
            if( mState != STATE_CONNECTED )
            {
                return;
            }
            connectedThread = mConnectedThreads.get( 0 );
        }
        connectedThread.write( out );
    }

    private void connectionFailed()
    {
        mHandler.obtainMessage( BluetoothConnection.MESSAGE_TOAST, "Unable to connect to device." ).sendToTarget();

        restart();
    }

    private void connectionLost( ConnectedThread connectedThread )
    {
        mHandler.obtainMessage( BluetoothConnection.MESSAGE_TOAST, "Device connection was lost" ).sendToTarget();
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
                temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord( MainActivity.SERVICE_NAME, MY_UUID );
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

            BluetoothSocket socket = null;

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
        private boolean mIsConnected;

        public ConnectedThread( BluetoothSocket socket )
        {
            Log.d( TAG, "BEGIN create ConnectedThread" );
            mSocket = socket;
            mIsConnected = true;

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
                        mHandler.obtainMessage( BluetoothConnection.MESSAGE_READ, bytes, -1, buffer ).sendToTarget();
                    }
                } catch( IOException io )
                {
                    Log.e( TAG, "failed to read, disconnected", io );
                    mIsConnected = false;
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
                mHandler.obtainMessage( BluetoothConnection.MESSAGE_WRITE, -1, -1, buffer ).sendToTarget();
            } catch( IOException io )
            {
                Log.e( TAG, "Exception during write", io );
            }
        }

        public void cancel()
        {
            mIsConnected = false;
            try
            {
                mSocket.close();
            } catch( IOException io )
            {
                Log.e( TAG, "close() of connected socket failed", io );
            }
        }

        public boolean isConnected()
        {
            return mIsConnected;
        }
    }
}
