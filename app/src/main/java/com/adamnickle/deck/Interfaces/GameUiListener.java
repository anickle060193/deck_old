package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public interface GameUiListener
{
    public boolean onAttemptSendCard( String ownerID, Card card );
    public boolean canSendCard( String ownerID, Card card );
}
