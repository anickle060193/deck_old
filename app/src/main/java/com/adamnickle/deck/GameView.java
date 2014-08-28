package com.adamnickle.deck;

import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.adamnickle.deck.Game.Card;

import java.util.Iterator;
import java.util.LinkedList;

public class GameView extends View
{
    private static final String TAG = "GameView";

    private GestureDetectorCompat mDetector;
    private final LinkedList<CardDrawable> mCardDrawables;
    private SparseArray<CardDrawable> mMovingCardDrawables;

    public GameView( GameActivity context )
    {
        super( context );

        mDetector = new GestureDetectorCompat( context, mGestureListener );
        mCardDrawables = new LinkedList< CardDrawable >();
        Card card;
        final int width = (int)( CardDrawable.DEFAULT_WIDTH * 0.5f );
        final int height = (int)( CardDrawable.DEFAULT_HEIGHT * 0.5f );
        for( int i = 0; i < 13; i++ )
        {
            card = new Card( i );
            mCardDrawables.addFirst( new CardDrawable( getResources(), card.getResource(), 300, 300, width, height, true ) );
        }
        mMovingCardDrawables = new SparseArray< CardDrawable >();
    }

    @Override
    protected void onAttachedToWindow()
    {
        postDelayed( mUpdateScreen, 10 );
    }

    private Runnable mUpdateScreen = new Runnable()
    {
        @Override
        public void run()
        {
            GameView.this.invalidate();

            postDelayed( this, 10 );
        }
    };

    @Override
    public void onDraw( Canvas canvas )
    {
        synchronized( mCardDrawables )
        {
            Iterator< CardDrawable > cardDrawableIterator = mCardDrawables.descendingIterator();
            CardDrawable cardDrawable;
            while( cardDrawableIterator.hasNext() )
            {
                cardDrawable = cardDrawableIterator.next();
                if( cardDrawable != null )
                {
                    cardDrawable.draw( canvas );
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        mDetector.onTouchEvent( event );

        final int action = MotionEventCompat.getActionMasked( event );
        switch( action )
        {
            case MotionEvent.ACTION_DOWN:
            {
                final int pointerIndex = MotionEventCompat.getActionIndex( event );
                final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );
                final float x = MotionEventCompat.getX( event, pointerIndex );
                final float y = MotionEventCompat.getY( event, pointerIndex );

                synchronized( mCardDrawables )
                {
                    CardDrawable activeCardDrawable = null;
                    for( CardDrawable cardDrawable : mCardDrawables )
                    {
                        if( cardDrawable != null && cardDrawable.contains( (int) x, (int) y ) )
                        {
                            activeCardDrawable = cardDrawable;
                            mMovingCardDrawables.put( pointerId, activeCardDrawable );
                            break;
                        }
                    }
                    if( activeCardDrawable != null )
                    {
                        mCardDrawables.removeFirstOccurrence( activeCardDrawable );
                        mCardDrawables.addFirst( activeCardDrawable );
                    }
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
            {
                final int pointerIndex = MotionEventCompat.getActionIndex( event );
                final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );
                final float x = MotionEventCompat.getX( event, pointerIndex );
                final float y = MotionEventCompat.getY( event, pointerIndex );

                synchronized( mCardDrawables )
                {
                    CardDrawable activeCardDrawable = null;
                    for( CardDrawable cardDrawable : mCardDrawables )
                    {
                        if( cardDrawable != null && cardDrawable.contains( (int) x, (int) y ) )
                        {
                            activeCardDrawable = cardDrawable;
                            mMovingCardDrawables.put( pointerId, activeCardDrawable );
                            break;
                        }
                    }
                    if( activeCardDrawable != null )
                    {
                        mCardDrawables.removeFirstOccurrence( activeCardDrawable );
                        mCardDrawables.addFirst( activeCardDrawable );
                    }
                }

                break;
            }

            case MotionEvent.ACTION_MOVE:
            {
                for( int i = 0; i < MotionEventCompat.getPointerCount( event ); i++ )
                {
                    final int pointerId = MotionEventCompat.getPointerId( event, i );
                    final float x = MotionEventCompat.getX( event, i );
                    final float y = MotionEventCompat.getY( event, i );

                    CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                    if( cardDrawable != null )
                    {
                        cardDrawable.update( (int) x, (int) y );
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            {
                mMovingCardDrawables.clear();
                break;
            }

            case MotionEvent.ACTION_UP:
            {
                final int pointerIndex = MotionEventCompat.getActionIndex( event );
                final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );
                mMovingCardDrawables.remove( pointerId );
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
            {
                final int pointerIndex = MotionEventCompat.getActionIndex( event );
                final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );

                mMovingCardDrawables.remove( pointerId );

                break;
            }
        }

        return true;
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener()
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            return true;
        }
    };
}
