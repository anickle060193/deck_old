package com.adamnickle.deck.spi;


public interface ConnectionInterface
{
    public void sendDataToDevice( int deviceID, byte[] data );
}
