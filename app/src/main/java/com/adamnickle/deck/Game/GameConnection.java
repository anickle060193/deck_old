package com.adamnickle.deck.Game;

import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.BluetoothConnectionListener;
import com.adamnickle.deck.spi.GameConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionListener;

public class GameConnection implements BluetoothConnectionListener, GameConnectionInterface
{
    public static final int MESSAGE_CARD = 1;
    public static final int MESSAGE_CARD_SEND_REQUEST = 2;

    private GameConnectionListener mGameConnectionListener;
    private ConnectionInterface mBluetoothConnection;
    /*
    public GameConnection( GameConnectionListener listener, BluetoothConnectionInterface bluetoothConnectionInterface )
    {
        mGameConnectionListener = listener;
        mBluetoothConnection = bluetoothConnectionInterface;
    }
    */
    public void setGameConnectionListener( GameConnectionListener gameConnectionListener )
    {
        mGameConnectionListener = gameConnectionListener;
    }

    public void setBluetoothConnectionInterface( ConnectionInterface connectionInterface )
    {
        mBluetoothConnection = connectionInterface;
    }

    /*******************************************************************
     * BluetoothConnectionListener Methods
     *******************************************************************/
    @Override
    public void onMessageReceive( int senderID, int bytes, byte[] data )
    {
        int messageType = (int) data[ 0 ];
        switch( messageType )
        {
            case MESSAGE_CARD:
                final int cardNumber = data[ 1 ];
                mGameConnectionListener.onCardReceive( senderID, new Card( cardNumber ) );
                break;

            case MESSAGE_CARD_SEND_REQUEST:
                mGameConnectionListener.onCardSendRequested( senderID ); //TODO Add something about valid cards
                break;
        }
    }

    @Override
    public void onDeviceConnect( int senderID, String deviceName )
    {
        mGameConnectionListener.onPlayerConnect( senderID, deviceName );
    }

    @Override
    public void onNotification( String notification )
    {
        mGameConnectionListener.onNotification( notification );
    }

    @Override
    public void onConnectionStateChange( int newState )
    {
        mGameConnectionListener.onConnectionStateChange( newState );
    }

    @Override
    public void onConnectionLost( int deviceID )
    {
        mGameConnectionListener.onPlayerDisconnect( deviceID );
    }

    /*******************************************************************
     * GameConnectionInterface Methods
     *******************************************************************/
    @Override
    public void sendCard( int toDeviceID, Card card )
    {
        byte[] data = { MESSAGE_CARD, (byte) card.getCardNumber() };
        mBluetoothConnection.sendDataToDevice( toDeviceID, data );
    }

    @Override
    public void requestCard( int fromDeviceID )
    {
        byte[] data = { MESSAGE_CARD_SEND_REQUEST };
        mBluetoothConnection.sendDataToDevice( fromDeviceID, data );
    }
}
