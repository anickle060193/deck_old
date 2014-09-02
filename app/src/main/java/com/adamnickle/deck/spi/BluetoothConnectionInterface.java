package com.adamnickle.deck.spi;


public interface BluetoothConnectionInterface
{
    public void sendDataToDevice( int deviceID, byte[] data );
}
