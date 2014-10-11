package com.adamnickle.deck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;


public class SlidingFrameLayout extends FrameLayout
{
    private enum State
    {
        COLLAPSED,
        EXPANDED,
        COLLAPSING,
        EXPANDING,
    }

    private State mCurrentState;
    private Animation mExpandAnimation;
    private Animation mCollapseAnimation;

    public SlidingFrameLayout( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );

        this.setVisibility( INVISIBLE );
        mCurrentState = State.COLLAPSED;

        mExpandAnimation = AnimationUtils.loadAnimation( context, R.anim.slide_view_in );
        mExpandAnimation.setAnimationListener( new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart( Animation animation )
            {
                SlidingFrameLayout.this.setVisibility( VISIBLE );
                mCurrentState = State.EXPANDING;
            }

            @Override
            public void onAnimationEnd( Animation animation )
            {
                mCurrentState = State.EXPANDED;
            }

            @Override
            public void onAnimationRepeat( Animation animation )
            {

            }
        } );

        mCollapseAnimation = AnimationUtils.loadAnimation( context, R.anim.slide_view_out );
        mCollapseAnimation.setAnimationListener( new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart( Animation animation )
            {
                mCurrentState = State.COLLAPSING;
            }

            @Override
            public void onAnimationEnd( Animation animation )
            {
                SlidingFrameLayout.this.setVisibility( GONE );
                mCurrentState = State.COLLAPSED;
            }

            @Override
            public void onAnimationRepeat( Animation animation )
            {

            }
        } );
    }

    public SlidingFrameLayout( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public SlidingFrameLayout( Context context )
    {
        this( context, null );
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
    }

    public void collapseFrame()
    {
        if( mCurrentState == State.EXPANDED )
        {
            this.startAnimation( mCollapseAnimation );
        }
    }

    public void expandFrame()
    {
        if( mCurrentState == State.COLLAPSED )
        {
            this.startAnimation( mExpandAnimation );
        }
    }

    public void toggleState()
    {
        if( mCurrentState == State.EXPANDED )
        {
            collapseFrame();
        }
        else if( mCurrentState == State.COLLAPSED )
        {
            expandFrame();
        }
    }

    public boolean isOpen()
    {
        return mCurrentState == State.EXPANDED;
    }
}
