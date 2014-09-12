package com.adamnickle.deck.Interfaces;

import com.adamnickle.deck.Game.Card;


public interface GameUiInterface
{
    public void addCardDrawable( Card card );
    public boolean removeCardDrawable( Card card );
    public void displayNotification( String notification );
}
