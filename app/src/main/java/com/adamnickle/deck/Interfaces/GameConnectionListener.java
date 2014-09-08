package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public interface GameConnectionListener
{
    public void onPlayerConnect( String deviceID, String deviceName );
    public void onPlayerDisconnect( String playerID );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onCardReceive( String senderID, Card card );
    public void onCardSendRequested( String requesterID );
    public void onReceivePlayerName( String senderID, String name );
    public void onClearPlayerHand( String senderID );
}
