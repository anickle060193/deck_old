package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Player;

public interface GameConnectionListener
{
    public void setGameConnection( GameConnection gameConnection );
    public void onPlayerConnect( Player newPlayer );
    public void onPlayerDisconnect( String playerID );
    public void onServerConnect( String deviceID, String deviceName );
    public void onServerDisconnect( String deviceID );
    public void onNotification( String notification );
    public void onConnectionStateChange( int newState );
    public void onCardReceive( String senderID, String receiverID, Card card );
    public void onCardRequested( String requesterID, String requesteeID );
    public void onClearPlayerHand( String commanderID, String commandeeID );
    public void onSetDealer( String setterID, String setID, boolean isDealer );
    public void onReceiverCurrentPlayers( String senderID, String receiverID, Player[] players );
}
