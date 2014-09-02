package com.adamnickle.deck.spi;


import com.adamnickle.deck.Game.Card;

public interface GameConnectionInterface
{
    public void requestCard( int fromDeviceID );
    public void sendCard( int toDeviceID, Card card );
}
