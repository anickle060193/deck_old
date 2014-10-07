package com.adamnickle.deck;


import android.app.Activity;
import android.graphics.Color;
import android.view.MotionEvent;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Interfaces.CardHolderListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TableView extends GameView
{
    private static final float SCALING_FACTOR = 0.5f;

    private int mNextX = 150;
    private int mNextY = 300;

    public TableView( Activity activity )
    {
        super( activity );
        setBackgroundColor( Color.parseColor( "#66CCFF" ) );
        this.setGameGestureListener( mGameGestureListener );
    }

    private void setNextXY( int cardWidth, int cardHeight )
    {
        mNextX += 250;
        if( ( mNextX + 150 ) > getWidth() )
        {
            mNextX = 150;
            mNextY += 300;
        }
    }

    @Override
    protected void setGameBackground( int drawableIndex )
    {
    }

    @Override
    public CardHolderListener getCardHolderListener()
    {
        return mTableCardHolderListener;
    }

    protected final CardHolderListener mTableCardHolderListener = new CardHolderListener()
    {
        @Override
        public void onCardRemoved( String playerID, Card card )
        {
            ArrayList<CardDrawable> cardDrawables = mCardDrawablesByOwners.get( playerID );
            if( cardDrawables != null )
            {
                CardDrawable removeCardDrawable = null;
                Iterator<CardDrawable> cardDrawableIterator = cardDrawables.iterator();
                while( cardDrawableIterator.hasNext() )
                {
                    CardDrawable cardDrawable = cardDrawableIterator.next();
                    if( cardDrawable.getCard().equals( card ) )
                    {
                        removeCardDrawable = cardDrawable;
                        cardDrawableIterator.remove();
                        break;
                    }
                }
                if( removeCardDrawable != null )
                {
                    mCardDrawables.remove( removeCardDrawable );
                }
            }
        }

        @Override
        public void onCardsRemoved( String playerID, Card[] cards )
        {
            Arrays.sort( cards, new Card.CardComparator( CardCollection.SortingType.SORT_BY_CARD_NUMBER ) );

            final ArrayList<CardDrawable> cardDrawables = mCardDrawablesByOwners.get( playerID );
            final Iterator<CardDrawable> cardDrawableIterator = cardDrawables.iterator();
            final ArrayList< CardDrawable > removedCards = new ArrayList< CardDrawable >();

            while( cardDrawableIterator.hasNext() && ( removedCards.size() < cards.length ) )
            {
                final CardDrawable cardDrawable = cardDrawableIterator.next();
                if( Arrays.binarySearch( cards, cardDrawable.getCard() ) >= 0 )
                {
                    removedCards.add( cardDrawable );
                    cardDrawableIterator.remove();
                }
            }
            mCardDrawables.removeAll( removedCards );
        }

        @Override
        public void onCardAdded( String playerID, Card card )
        {
            final CardDrawable cardDrawable = new CardDrawable( TableView.this, mListener, playerID, card, mNextX, mNextY, SCALING_FACTOR );

            setNextXY( cardDrawable.getWidth(), cardDrawable.getHeight() );

            cardDrawable.flipFaceUp();
            mCardDrawables.addFirst( cardDrawable );

            ArrayList<CardDrawable> cardDrawables;
            if( !mCardDrawablesByOwners.containsKey( playerID ) )
            {
                cardDrawables = new ArrayList< CardDrawable >();
                mCardDrawablesByOwners.put( playerID, cardDrawables );
            }
            else
            {
                cardDrawables = mCardDrawablesByOwners.get( playerID );
            }
            cardDrawables.add( cardDrawable );
        }

        @Override
        public void onCardsAdded( String playerID, Card[] cards )
        {
            ArrayList<CardDrawable> cardDrawables;
            if( !mCardDrawablesByOwners.containsKey( playerID ) )
            {
                cardDrawables = new ArrayList< CardDrawable >();
                mCardDrawablesByOwners.put( playerID, cardDrawables );
            }
            else
            {
                cardDrawables = mCardDrawablesByOwners.get( playerID );
            }


            for( Card card : cards )
            {
                final CardDrawable cardDrawable = new CardDrawable( TableView.this, mListener, playerID, card, SCALING_FACTOR );
                mCardDrawables.add( cardDrawable );
                cardDrawables.add( cardDrawable );
            }
        }

        @Override
        public void onCardsCleared( String cardHolderID )
        {
            final ArrayList< CardDrawable > cardDrawables = mCardDrawablesByOwners.remove( cardHolderID );
            if( cardDrawables != null )
            {
                mCardDrawables.removeAll( cardDrawables );
            }
        }
    };

    private GameGestureListener mGameGestureListener = new GameGestureListener()
    {
        @Override
        public boolean onCardFling( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float velocityX, float velocityY )
        {
            return false;
        }

        @Override
        public boolean onCardMove( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float x, float y )
        {
            return false;
        }

        @Override
        public boolean onCardSingleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            return super.onCardSingleTap( event, cardDrawable );
        }

        @Override
        public boolean onCardDoubleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            if( mListener.canSendCard( cardDrawable.getOwnerID(), cardDrawable.getCard() ) )
            {
                return mListener.onAttemptSendCard( cardDrawable.getOwnerID(), cardDrawable.getCard() );
            }
            return false;
        }

        @Override
        public boolean onGameDoubleTap( MotionEvent event )
        {
            return false;
        }
    };
}
