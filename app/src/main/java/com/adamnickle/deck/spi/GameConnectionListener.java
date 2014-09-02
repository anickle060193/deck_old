package com.adamnickle.deck.spi;


import com.adamnickle.deck.Game.Card;

public interface GameConnectionListener
{
    public void onPlayerConnect( int deviceID, String deviceName );
    public void onPlayerDisconnect( int deviceID );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onCardReceive( int senderID, Card card );
    public void onCardSendRequested( int requesterID );
}
