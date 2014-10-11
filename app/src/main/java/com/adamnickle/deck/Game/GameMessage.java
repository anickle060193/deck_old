package com.adamnickle.deck.Game;


import com.crashlytics.android.Crashlytics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;

public class GameMessage extends EnumMap< GameMessage.Key, Object >
{
    public enum MessageType
    {
        MESSAGE_RECEIVE_CARD,
        MESSAGE_RECEIVE_CARDS,
        MESSAGE_REMOVE_CARD,
        MESSAGE_REMOVE_CARDS,
        MESSAGE_CLEAR_CARDS,
        MESSAGE_NEW_PLAYER,
        MESSAGE_SET_NAME,
        MESSAGE_PLAYER_LEFT,
        MESSAGE_SET_DEALER,
        MESSAGE_CARD_HOLDERS,
    }

    protected enum Key
    {
        MESSAGE_TYPE,
        ORIGINAL_SENDER_ID,
        RECEIVER_ID,
        CARD_NUMBER,
        CARD_NUMBERS,
        PLAYER_NAME,
        IS_DEALER,
        CURRENT_PLAYER_IDS,
        CURRENT_PLAYER_NAMES,
        REMOVED_FROM_ID,
    }

    public boolean Handled;

    public GameMessage( final MessageType messageType, final String originalSenderID, final String receiverID )
    {
        super( Key.class );
        put( Key.MESSAGE_TYPE, messageType );
        put( Key.ORIGINAL_SENDER_ID, originalSenderID );
        put( Key.RECEIVER_ID, receiverID );
        Handled = false;
    }

    @Override
    public String toString()
    {
        try
        {
            StringBuilder s = new StringBuilder();
            s.append( "@" ).append( System.identityHashCode( this ) ).append( " =\n{\n\t" );
            for( Entry< Key, Object > entry : this.entrySet() )
            {
                final Key key = entry.getKey();
                final Object object = entry.getValue();
                s.append( key ).append( " = " );
                switch( key )
                {
                    case CARD_NUMBERS:
                        final int[] cardNumbers = (int[]) object;
                        for( int cardNumber : cardNumbers )
                        {
                            s.append( cardNumber ).append( ", " );
                        }
                        break;

                    case CURRENT_PLAYER_IDS:
                    case CURRENT_PLAYER_NAMES:
                        final String[] objects = (String[]) object;
                        for( String string : objects )
                        {
                            s.append( string ).append( ", " );
                        }
                        break;

                    default:
                        if( object != null )
                        {
                            s.append( object.toString() ).append( "," );
                        }
                        break;
                }
                s.append( "\n\t" );
            }
            s.deleteCharAt( s.length() - 1 ).append( "}" );
            return s.toString();
        }
        catch( Exception exception )
        {
            Crashlytics.logException( exception );
            return super.toString();
        }
    }

    public static byte[] serializeMessage( final GameMessage gameMessage )
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

    public void setReceiverID( final String receiverID )
    {
        super.put( Key.RECEIVER_ID, receiverID );
    }

    public Card getCard()
    {
        return new Card( (Integer) super.get( Key.CARD_NUMBER ) );
    }

    public void putCard( final Card card )
    {
        super.put( Key.CARD_NUMBER, card.getCardNumber() );
    }

    public String getPlayerName()
    {
        return (String) super.get( Key.PLAYER_NAME );
    }

    public void putName( final String name )
    {
        super.put( Key.PLAYER_NAME, name );
    }

    public boolean getIsDealer()
    {
        return (Boolean) super.get( Key.IS_DEALER );
    }

    public void putIsDealer( final boolean isDealer )
    {
        super.put( Key.IS_DEALER, isDealer );
    }

    public CardHolder[] getCardHolders()
    {
        final String[] playerIDs = (String[]) super.get( Key.CURRENT_PLAYER_IDS );
        final String[] playerNames = (String[]) super.get( Key.CURRENT_PLAYER_NAMES );
        final CardHolder[] players = new CardHolder[ playerIDs.length ];
        for( int i = 0; i < playerIDs.length; i++ )
        {
            players[ i ] = new CardHolder( playerIDs[ i ], playerNames[ i ] );
        }
        return players;
    }

    public void putCardHolders( final CardHolder[] players )
    {
        final String[] playerIDs = new String[ players.length ];
        final String[] playerNames = new String[ players.length ];
        for( int i = 0; i < players.length; i++ )
        {
            playerIDs[ i ] = players[ i ].getID();
            playerNames[ i ] = players[ i ].getName();
        }

        super.put( Key.CURRENT_PLAYER_IDS, playerIDs );
        super.put( Key.CURRENT_PLAYER_NAMES, playerNames );
    }

    public Card[] getCards()
    {
        final int[] cardNumbers = (int[]) super.get( Key.CARD_NUMBERS );
        Card[] cards = new Card[ cardNumbers.length ];
        for( int i = 0; i < cardNumbers.length; i++ )
        {
            cards[ i ] = new Card( cardNumbers[ i ] );
        }
        return cards;
    }

    public void putCards( final Card[] cards )
    {
        final int[] cardNumbers = new int[ cards.length ];
        for( int i = 0; i < cards.length; i++ )
        {
            cardNumbers[ i ] = cards[ i ].getCardNumber();
        }
        super.put( Key.CARD_NUMBERS, cardNumbers );
    }

    public String getRemovedFromID()
    {
        return (String) super.get( Key.REMOVED_FROM_ID );
    }

    public void putRemovedFromID( String removedFromID )
    {
        super.put( Key.REMOVED_FROM_ID, removedFromID );
    }
}
