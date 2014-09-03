package com.adamnickle.deck.spi;


public interface ConnectionListener
{
    public void onMessageReceive( int senderID, int bytes, byte[] data );
    public void onDeviceConnect( int senderID, String deviceName );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onConnectionLost( int deviceID );
}
