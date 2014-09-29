package com.adamnickle.deck;


import android.app.Activity;
import android.graphics.Color;
import android.view.MotionEvent;

import com.adamnickle.deck.Game.Card;

public class TableView extends GameView
{
    public TableView( Activity activity )
    {
        super( activity );
        setBackgroundColor( Color.parseColor( "#66CCFF" ) );
    }

    @Override
    protected void setGameBackground( int drawableIndex )
    {
    }

    private GameGestureListener mGameGestureListener = new GameGestureListener()
    {
        @Override
        public boolean onCardFling( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float velocityX, float velocityY )
        {
            return true;
        }

        @Override
        public boolean onCardMove( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float x, float y )
        {
            return true;
        }

        @Override
        public boolean onCardSingleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            mListener.onAttemptSendCard( cardDrawable.getOwnerID(), cardDrawable.getCard() );
            return true;
        }

        @Override
        public boolean onCardDoubleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            return true;
        }

        @Override
        public boolean onGameDoubleTap( MotionEvent event )
        {
            return true;
        }
    };

    @Override
    public synchronized void resetCard( String cardHolderID, Card card )
    {
    }

    @Override
    public synchronized void layoutCards( String cardHolderID )
    {
    }
}
