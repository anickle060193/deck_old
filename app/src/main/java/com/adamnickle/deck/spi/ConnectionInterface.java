package com.adamnickle.deck.spi;


public interface ConnectionInterface
{
    public static final int CONNECTION_TYPE_NONE = 0;
    public static final int CONNECTION_TYPE_SERVER = 1;
    public static final int CONNECTION_TYPE_CLIENT = 2;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTED_LISTENING = 4;

    public static final int LOCAL_DEVICE_ID = -1;

    public void setConnectionListener( ConnectionListener connectionListener );
    public void sendDataToDevice( int deviceID, byte[] data );
    public int getConnectionType();
    public int getState();
    public void findServer();
    public void connect( Object device );
    public void startConnection( int connectionType );
    public void restartConnection();
    public void finishConnecting();
    public void stopConnection();
}
