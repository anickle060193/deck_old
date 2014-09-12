package com.adamnickle.deck.Game;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class GameMessage extends HashMap<GameMessage.Key, Object>
{
    public enum MessageType
    {
        MESSAGE_CARD,
        MESSAGE_CARD_REQUEST,
        MESSAGE_CLEAR_HAND,
        MESSAGE_NEW_PLAYER,
        MESSAGE_PLAYER_LEFT,
        MESSAGE_SET_DEALER,
    }

    protected enum Key
    {
        MESSAGE_TYPE,
        ORIGINAL_SENDER_ID,
        RECEIVER_ID,
        CARD_NUMBER,
        PLAYER_NAME,
        IS_DEALER,
    }

    public GameMessage( MessageType messageType, String originalSenderID, String receiverID )
    {
        put( Key.MESSAGE_TYPE, messageType );
        put( Key.ORIGINAL_SENDER_ID, originalSenderID );
        put( Key.RECEIVER_ID, receiverID );
    }

    public static byte[] serializeMessage( GameMessage gameMessage )
    {
        byte[] data = null;
        try
        {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream( byteOutput );
            output.writeObject( gameMessage );
            data = byteOutput.toByteArray();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        return data;
    }

    public static GameMessage deserializeMessage( byte[] data )
    {
        GameMessage message = null;
        try
        {
            ByteArrayInputStream byteInput = new ByteArrayInputStream( data );
            ObjectInputStream input = new ObjectInputStream( byteInput );
            message = (GameMessage) input.readObject();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        catch( ClassNotFoundException e )
        {
            e.printStackTrace();
        }
        return message;
    }

    public MessageType getMessageType()
    {
        return (MessageType) get( Key.MESSAGE_TYPE );
    }

    public String getOriginalSenderID()
    {
        return (String) get( Key.ORIGINAL_SENDER_ID );
    }

    public String getReceiverID()
    {
        return (String) get( Key.RECEIVER_ID );
    }

    public void setReceiverID( String receiverID )
    {
        super.put( Key.RECEIVER_ID, receiverID );
    }

    public Card getCard()
    {
        return new Card( (Integer) super.get( Key.CARD_NUMBER ) );
    }

    public void putCard( Card card )
    {
        super.put( Key.CARD_NUMBER, card.getCardNumber() );
    }

    public String getPlayerName()
    {
        return (String) super.get( Key.PLAYER_NAME );
    }

    public void putName( String name )
    {
        super.put( Key.PLAYER_NAME, name );
    }

    public boolean getIsDealer()
    {
        return (Boolean) super.get( Key.IS_DEALER );
    }

    public void setIsDealer( boolean isDealer )
    {
        super.put( Key.IS_DEALER, isDealer );
    }
}
