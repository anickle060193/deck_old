package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public interface GameUiListener
{
    public boolean onAttemptSendCard( Card card );
    public boolean canSendCard();
    public void setGameUiInterface( GameUiInterfaceView gameUiInterfaceView );
}
