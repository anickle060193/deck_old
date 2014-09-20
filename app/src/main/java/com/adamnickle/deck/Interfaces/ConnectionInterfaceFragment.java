package com.adamnickle.deck.Interfaces;


import android.app.Fragment;

public abstract class ConnectionInterfaceFragment extends Fragment
{
    public static final String EXTRA_CONNECTION_TYPE = "EXTRA_CONNECTION_TYPE";
    public static final String EXTRA_CONNECTION_CLASS_NAME = "EXTRA_CONNECTION_CLASS_NAME";

    public static final int CONNECTION_TYPE_NONE = 0;
    public static final int CONNECTION_TYPE_SERVER = 1;
    public static final int CONNECTION_TYPE_CLIENT = 2;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTED_LISTENING = 4;

    public abstract void setConnectionListener( ConnectionListener connectionListener );
    public abstract void sendDataToDevice( String deviceID, byte[] data );
    public abstract boolean isConnected();
    public abstract String getLocalDeviceID();
    public abstract String getLocalDeviceName();
    public abstract void setConnectionType( int connectionType );
    public abstract int getConnectionType();
    public abstract int getState();
    public abstract void findServer();
    public abstract void connect( Object device );
    public abstract void startConnection();
    public abstract void restartConnection();
    public abstract void finishConnecting();
    public abstract void stopConnection();
}
