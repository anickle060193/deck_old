package com.adamnickle.deck.spi;


public interface ConnectionListener
{
    public void onMessageReceive( String senderAddress, int bytes, byte[] data );
    public void onDeviceConnect( String deviceAddress, String deviceName );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onConnectionLost( String deviceAddress );
    public void onConnectionFailed();
}
