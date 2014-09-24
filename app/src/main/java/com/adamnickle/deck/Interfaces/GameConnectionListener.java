package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Player;

public interface GameConnectionListener
{
    public void setGameConnection( GameConnection gameConnection );
    public void onPlayerConnect( Player newPlayer );
    public void onPlayerNameReceive( String senderID, String newName );
    public void onPlayerDisconnect( String playerID );
    public void onServerConnect( String deviceID, String deviceName );
    public void onServerDisconnect( String deviceID );
    public void onNotification( String notification );
    public void onConnectionStateChange( Connection.State newState );
    public void onCardReceive( String senderID, String receiverID, Card card );
    public void onCardsReceive( String senderID, String receiverID, Card[] cards );
    public void onCardRequested( String requesterID, String requesteeID );
    public void onClearPlayerHand( String commanderID, String commandeeID );
    public void onSetDealer( String setterID, String setID, boolean isDealer );
    public void onReceiverCurrentPlayers( String senderID, String receiverID, Player[] players );
}
