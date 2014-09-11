package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public interface GameUiListener
{
    public boolean onAttemptSendCard( Card card );
    public void setGameUiInterface( GameUiInterface gameUiInterface );
}
