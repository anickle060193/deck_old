package com.adamnickle.deck.Interfaces;


import android.support.v4.app.Fragment;

public abstract class ConnectionFragment extends Fragment
{
    public static final String EXTRA_CONNECTION_TYPE = "EXTRA_CONNECTION_TYPE";
    public static final String EXTRA_CONNECTION_CLASS_NAME = "EXTRA_CONNECTION_CLASS_NAME";

    public static enum ConnectionType
    {
        NONE,
        SERVER,
        CLIENT,
    }

    public static enum State
    {
        NONE,
        LISTENING,
        CONNECTING,
        CONNECTED,
        CONNECTED_LISTENING,
    }

    public abstract void setConnectionListener( ConnectionListener connectionListener );
    public abstract void sendDataToDevice( String deviceID, byte[] data );
    public abstract boolean isConnected();
    public abstract String getLocalDeviceID();
    public abstract String getLocalDeviceName();
    public abstract void setConnectionType( ConnectionType connectionType );
    public abstract ConnectionType getConnectionType();
    public abstract State getState();
    public abstract void findServer();
    public abstract void connect( Object device );
    public abstract void startConnection();
    public abstract void restartConnection();
    public abstract void finishConnecting();
    public abstract void stopConnection();
    public abstract boolean isPlayerID( String ID );
}
