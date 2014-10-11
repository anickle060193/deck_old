package com.adamnickle.deck;


import android.app.Activity;
import android.view.MotionEvent;

import com.adamnickle.deck.Game.Card;

public class TableView extends GameView
{
    private static final float SCALING_FACTOR = 0.5f;
    private static float FLING_VELOCITY = 400;

    private final SlidingFrameLayout mSlidingTable;

    public TableView( Activity activity )
    {
        super( activity );
        this.setGameGestureListener( mGameGestureListener );
        mSlidingTable = (SlidingFrameLayout) activity.findViewById( R.id.table );
        FLING_VELOCITY *= getResources().getDisplayMetrics().density;
    }

    @Override
    protected void setGameBackground( int drawableIndex )
    {
    }

    @Override
    protected CardDrawable createCardDrawable( String cardHolderID, Card card )
    {
        final CardDrawable cardDrawable = new CardDrawable( TableView.this, mListener, cardHolderID, card, SCALING_FACTOR );
        cardDrawable.flipFaceUp();
        return cardDrawable;
    }

    private GameGestureListener mGameGestureListener = new GameGestureListener()
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            return true;
        }

        @Override
        public boolean onGameDoubleTap( MotionEvent event )
        {
            return true;
        }

        @Override
        public boolean onBackgroundFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY )
        {
            if( velocityY < -1.0f * FLING_VELOCITY && Math.abs( velocityX ) < FLING_VELOCITY )
            {
                mSlidingTable.collapseFrame();
            }
            return true;
        }
    };
}
