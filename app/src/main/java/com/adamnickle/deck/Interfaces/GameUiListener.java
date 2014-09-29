package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public interface GameUiListener
{
    public boolean onAttemptSendCard( String senderID, Card card );
    public boolean canSendCard( String senderID, Card card );
    public void setGameUiInterface( GameUiView gameUiView );
}
