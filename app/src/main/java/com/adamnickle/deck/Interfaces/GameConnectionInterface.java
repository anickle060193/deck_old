package com.adamnickle.deck.Interfaces;


import com.adamnickle.deck.Game.Card;

public interface GameConnectionInterface
{
    public void setConnectionType( int connectionType );
    public void setConnectionInterface( ConnectionInterfaceFragment connectionInterfaceFragment );
    public void requestCard( String fromDeviceAddress );
    public void sendCard( String toDeviceAddress, Card card );
    public void clearPlayerHand( String deviceID );
    public String getDefaultLocalPlayerID();
    public String getDefaultLocalPlayerName();
}
