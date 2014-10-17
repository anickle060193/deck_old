package com.adamnickle.deck;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;


public class CardDisplayLayout extends RelativeLayout
{
    private static final String TAG = CardDisplayLayout.class.getSimpleName();

    private GestureDetector mDetector;

    public CardDisplayLayout( Context context )
    {
        this( context, null );
    }

    public CardDisplayLayout( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public CardDisplayLayout( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );

        mDetector = new GestureDetector( getContext(), mGestureListener );
    }

    private boolean contains( View view, float x, float y )
    {
        Log.d( TAG, "contains: Left: " + view.getLeft() + " Right:  " + view.getRight() + " Top:  " + view.getTop() + " Bottom:  " + view.getBottom() );
        Log.d( TAG, "X: " + x + " Y: " + y );
        return view.getLeft() <= x && view.getRight() >= x && view.getTop() <= y && view.getBottom() >= y;
    }

    private View getChildUnderPoint( float x, float y )
    {
        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View child = getChildAt( i );
            if( contains( child, x, y ) )
            {
                return child;
            }
        }
        return null;
    }

    @Override
    public boolean onInterceptTouchEvent( MotionEvent event )
    {
        return true;
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        return mDetector.onTouchEvent( event );
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener()
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            final View child = getChildUnderPoint( event.getX(), event.getY() );
            if( child != null )
            {
                child.bringToFront();
            }
            return true;
        }

        @Override
        public boolean onScroll( MotionEvent event1, MotionEvent event2, float distanceX, float distanceY )
        {
            final View child = getChildUnderPoint( event1.getX(), event1.getY() );
            if( child != null )
            {
                child.setX( (int) ( child.getX() - distanceX ) );
                child.setY( (int) ( child.getY() - distanceY ) );
            }
            return true;
        }

        @Override
        public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY )
        {
            return super.onFling( e1, e2, velocityX, velocityY );
        }
    };
}
