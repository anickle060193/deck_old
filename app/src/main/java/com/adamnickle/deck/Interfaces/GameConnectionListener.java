package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Game.GameMessage;

public interface GameConnectionListener
{
    public void setGameConnection( GameConnection gameConnection );
    public boolean canHandleMessage( GameMessage message );
    public void onCardHolderConnect( String ID, String name );
    public void onCardHolderNameReceive( String senderID, String newName );
    public void onCardHolderDisconnect( String ID );
    public void onGameStarted();
    public void onServerConnect( String deviceID, String deviceName );
    public void onServerDisconnect( String deviceID );
    public void onNotification( String notification );
    public void onConnectionStateChange( Connection.State newState );
    public void onCardReceive( String senderID, String receiverID, Card card );
    public void onCardsReceive( String senderID, String receiverID, Card[] cards );
    public void onCardRemove( String removerID, String removedID, Card card );
    public void onCardRequested( String requesterID, String requesteeID );
    public void onClearCards( String commanderID, String commandeeID );
    public void onSetDealer( String setterID, String setID, boolean isDealer );
    public void onReceiveCardHolders( String senderID, String receiverID, CardHolder[] cardHolders );
}
