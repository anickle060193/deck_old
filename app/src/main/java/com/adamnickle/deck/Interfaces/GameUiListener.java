package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.CardDisplayLayout;
import com.adamnickle.deck.Game.Card;

public interface GameUiListener
{
    public boolean onAttemptSendCard( String ownerID, Card card, CardDisplayLayout.Side side );
    public boolean canSendCard( String ownerID, Card card );
}
