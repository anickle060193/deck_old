package com.adamnickle.deck;


import com.adamnickle.deck.Game.Card;

import java.util.Arrays;

public class GameMessageParser
{
    public static final int MESSAGE_CARD = 1;
    public static final int MESSAGE_CARD_SEND_REQUEST = 2;
    public static final int MESSAGE_PLAYER_NAME = 3;
    public static final int MESSAGE_CLEAR_HAND = 4;

    private GameMessageParser() { }

    /*******************************************************************
     * Message Create Methods
     *******************************************************************/
    public static byte[] createCardMessage( Card card )
    {
        return new byte[]{ MESSAGE_CARD, (byte) card.getCardNumber() };
    }

    public static byte[] createCardRequestMessage()
    {
        return new byte[]{ MESSAGE_CARD_SEND_REQUEST };
    }
    
    public static byte[] createPlayerNameMessage( String name )
    {
        final byte nameBytes[] = name.getBytes();
        final byte ret[] = new byte[ nameBytes.length + 1 ];
        ret[0] = MESSAGE_PLAYER_NAME;
        System.arraycopy( nameBytes, 0, ret, 1, nameBytes.length );
        return ret;
    }

    public static byte[] createClearHandMessage()
    {
        return new byte[]{ MESSAGE_CLEAR_HAND };
    }

    /*******************************************************************
     * Message Parse Methods
     *******************************************************************/
    public static Card parseCardMessage( byte[] data )
    {
        return new Card( data[ 1 ] );
    }

    public static int parseCardSendRequestMessage( byte[] data )
    {
        return MESSAGE_CARD_SEND_REQUEST;
    }

    public static String parsePlayerNameMessage( byte[] data )
    {
        return new String( Arrays.copyOfRange( data, 1, data.length - 1 ) );
    }

    public static int parseClearHandMessage( byte[] data )
    {
        return MESSAGE_CLEAR_HAND;
    }
}
