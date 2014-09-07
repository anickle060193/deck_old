package com.adamnickle.deck.spi;


public interface ConnectionListener
{
    public void onMessageReceive( String senderID, int bytes, byte[] data );
    public void onDeviceConnect( String deviceID, String deviceName );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onConnectionLost( String deviceID );
    public void onConnectionFailed();
}
