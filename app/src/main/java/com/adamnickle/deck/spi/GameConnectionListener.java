package com.adamnickle.deck.spi;


import com.adamnickle.deck.Game.Card;

public interface GameConnectionListener
{
    public void onPlayerConnect( String deviceAddress, String deviceName );
    public void onPlayerDisconnect( String senderAddress );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onCardReceive( String senderAddress, Card card );
    public void onCardSendRequested( String requesterAddress );
}
