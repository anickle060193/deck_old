package com.adamnickle.deck.spi;


import com.adamnickle.deck.Game.Card;

public interface GameConnectionInterface
{
    public void setConnectionType( int connectionType );
    public void setConnectionInterface( ConnectionInterfaceFragment connectionInterfaceFragment );
    public void requestCard( String fromDeviceAddress );
    public void sendCard( String toDeviceAddress, Card card );
}
