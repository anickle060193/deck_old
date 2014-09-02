package com.adamnickle.deck.spi;


import com.adamnickle.deck.Game.Card;

public interface GameUiListener
{
    public boolean onAttemptSendCard( Card card );
}
