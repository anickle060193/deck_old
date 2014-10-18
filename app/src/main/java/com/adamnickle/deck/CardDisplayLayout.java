package com.adamnickle.deck;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


public class CardDisplayLayout extends FrameLayout
{
    public enum Side
    {
        LEFT, TOP, RIGHT, BOTTOM, NONE
    }

    private ViewDragHelper mDragHelper;
    private GestureDetector mDetector;
    private boolean mPreventLayout;

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

        mDragHelper = ViewDragHelper.create( this, new DragHelperCallback() );
        mDetector = new GestureDetector( getContext(), new CardDisplayGestureListener() );
        mPreventLayout = false;
    }

    @Override
    public boolean onInterceptTouchEvent( MotionEvent event )
    {
        return mDragHelper.shouldInterceptTouchEvent( event );
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        mDragHelper.processTouchEvent( event );
        mDetector.onTouchEvent( event );
        return true;
    }

    @Override
    public void requestLayout()
    {
        if( !mPreventLayout )
        {
            super.requestLayout();
        }
    }

    public void childViewOffScreen( PlayingCardView playingCardView )
    {
        Log.d( "CardDisplayLayout", "childViewOffScreen" );
        playingCardView.reset();
    }

    public boolean childHitWall( PlayingCardView playingCardView, Side wall )
    {
        return false;
    }

    public class DragHelperCallback extends ViewDragHelper.Callback
    {
        @Override
        public boolean tryCaptureView( View view, int i )
        {
            return view instanceof PlayingCardView;
        }

        @Override
        public void onViewCaptured( View capturedChild, int activePointerId )
        {
            super.onViewCaptured( capturedChild, activePointerId );
            mPreventLayout = true;
            CardDisplayLayout.this.bringChildToFront( capturedChild );
            mPreventLayout = false;
            ( (PlayingCardView) capturedChild ).onTouched();
        }

        @Override
        public int clampViewPositionHorizontal( View child, int left, int dx )
        {
            final float minLeft = -child.getWidth() / 2.0f;
            final float maxLeft = CardDisplayLayout.this.getWidth() - child.getWidth() / 2.0f;
            return (int) Math.max( minLeft, Math.min( left, maxLeft ) );
        }

        @Override
        public int clampViewPositionVertical( View child, int top, int dy )
        {
            final float minTop = -child.getHeight() / 2.0f;
            final float maxTop = CardDisplayLayout.this.getHeight() - child.getHeight() / 2.0f;
            return (int) Math.max( minTop, Math.min( top, maxTop ) );
        }

        @Override
        public void onViewReleased( View releasedChild, float xVelocity, float yVelocity )
        {
            ( (PlayingCardView) releasedChild ).fling( xVelocity, yVelocity );
        }
    }

    public class CardDisplayGestureListener implements GestureDetector.OnGestureListener
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            return true;
        }

        @Override
        public void onShowPress( MotionEvent event )
        {

        }

        @Override
        public boolean onSingleTapUp( MotionEvent event )
        {
            View view = mDragHelper.findTopChildUnder( (int) event.getX(), (int) event.getY() );
            if( view != null )
            {
                if( view instanceof PlayingCardView )
                {
                    ( (PlayingCardView) view ).flipFaceUp();
                }
            }
            return true;
        }

        @Override
        public boolean onScroll( MotionEvent event, MotionEvent event2, float v, float v2 )
        {
            return false;
        }

        @Override
        public void onLongPress( MotionEvent event )
        {

        }

        @Override
        public boolean onFling( MotionEvent event, MotionEvent event2, float v, float v2 )
        {
            return false;
        }
    }
}
