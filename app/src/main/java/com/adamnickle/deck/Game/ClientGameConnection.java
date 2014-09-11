package com.adamnickle.deck.Game;

import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnection;
import com.adamnickle.deck.Interfaces.GameConnectionListener;


public class ClientGameConnection extends GameConnection
{
    private String mActualServerAddress;

    public ClientGameConnection( ConnectionInterfaceFragment connection, GameConnectionListener listener )
    {
        super( connection, listener );
    }

    /*******************************************************************
     * ConnectionListener Methods
     *******************************************************************/
    @Override
    public void onDeviceConnect( String deviceID, String deviceName )
    {
        mActualServerAddress = deviceID;
    }

    /*******************************************************************
     * GameConnection Methods
     *******************************************************************/
    @Override
    public void startGame()
    {
        if( !isGameStarted() )
        {
            mConnection.findServer();
        }
    }

    @Override
    public void requestCard( String requesterID, String requesteeID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD_REQUEST, requesterID, requesteeID );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void sendCard( String senderID, String receiverID, Card card )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CARD, senderID, receiverID );
        message.putCard( card );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }

    @Override
    public void clearPlayerHand( String commandingDeviceID, String toBeClearedDeviceID )
    {
        final GameMessage message = new GameMessage( GameMessage.MessageType.MESSAGE_CLEAR_HAND, commandingDeviceID, toBeClearedDeviceID );
        final byte[] data = GameMessage.serializeMessage( message );
        mConnection.sendDataToDevice( mActualServerAddress, data );
    }
}
