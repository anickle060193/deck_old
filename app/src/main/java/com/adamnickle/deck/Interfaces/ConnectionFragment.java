package com.adamnickle.deck.Interfaces;


import android.app.Activity;
import android.support.v4.app.Fragment;

public abstract class ConnectionFragment extends Fragment
{
    public static final String EXTRA_CONNECTION_TYPE = "EXTRA_CONNECTION_TYPE";
    public static final String EXTRA_CONNECTION_CLASS_NAME = "EXTRA_CONNECTION_CLASS_NAME";

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRA_RETRYING_FIND = "retrying_find";

    public static final int RESULT_BLUETOOTH_NOT_SUPPORTED = Activity.RESULT_FIRST_USER;
    public static final int RESULT_BLUETOOTH_NOT_ENABLED = RESULT_BLUETOOTH_NOT_SUPPORTED + 1;
    public static final int RESULT_BLUETOOTH_DISABLED = RESULT_BLUETOOTH_NOT_ENABLED + 1;
    public static final int RESULT_NOT_CONNECTED_TO_DEVICE = RESULT_BLUETOOTH_DISABLED + 1;
    public static final int RESULT_SERVER_CLOSED = RESULT_NOT_CONNECTED_TO_DEVICE + 1;
    public static final int RESULT_COULD_NOT_CONNECT_TO_SERVER = RESULT_SERVER_CLOSED + 1;

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
