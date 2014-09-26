package com.adamnickle.deck.Game;


public class CardPile extends Player
{
    private static final String CARD_PILE_ID = "card_pile_";
    private static int CARD_PILE_COUNT = 0;

    private final int mMaxCards;

    public CardPile( String cardPileName, int maxCards )
    {
        super( CARD_PILE_ID + Integer.toString( CARD_PILE_COUNT++ ), cardPileName );

        mMaxCards = maxCards;
    }

    @Override
    public boolean addCard( Card card )
    {
        return ( getCardCount() + 1 ) <= mMaxCards && super.addCard( card );
    }

    @Override
    public boolean addCards( Card[] cards )
    {
        return ( getCardCount() + cards.length ) < mMaxCards && super.addCards( cards );
    }

    public void setFaceUp( Card card, boolean faceUp )
    {
        mListener.onSetFaceUp( mID, card, faceUp );
    }
}
