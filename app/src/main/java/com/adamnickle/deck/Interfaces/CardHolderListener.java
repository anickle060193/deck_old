package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public abstract class CardHolderListener
{
    public void onCardRemoved( String cardHolderID, Card card ) { }
    public void onCardsRemoved( String cardHolderID, Card[] cards ) { }
    public void onCardAdded( String cardHolderID, Card card ) { }
    public void onCardsAdded( String cardHolderID, Card[] cards ) { }
    public void onCardsCleared( String cardHolderID ) { }
    public void onSetFaceUp( String cardHolderID, Card card, boolean faceUp ) { }
}
