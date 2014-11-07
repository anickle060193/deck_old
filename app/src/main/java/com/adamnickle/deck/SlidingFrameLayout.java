package com.adamnickle.deck;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;


public class SlidingFrameLayout extends FrameLayout
{
    private enum State
    {
        COLLAPSED,
        COLLAPSING,
        EXPANDED,
        EXPANDING,
    }

    private static final int ANIMATOR_DURATION = 500;

    private State mCurrentState;
    private final ObjectAnimator mSlidingAnimator;

    public SlidingFrameLayout( Context context )
    {
        this( context, null );
    }

    public SlidingFrameLayout( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public SlidingFrameLayout( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );

        this.setFocusableInTouchMode( true );
        mCurrentState = State.COLLAPSED;

        mSlidingAnimator = ObjectAnimator.ofFloat( this, "Y", 0.0f, 0.0f )
                .setDuration( ANIMATOR_DURATION );
        mSlidingAnimator.addListener( new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd( Animator animation )
            {
                switch( mCurrentState )
                {
                    case EXPANDING:
                        mCurrentState = State.EXPANDED;
                        break;

                    case COLLAPSING:
                        mCurrentState = State.COLLAPSED;
                        break;
                }
            }
        } );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );

        switch( mCurrentState )
        {
            case EXPANDING:
                mCurrentState = State.EXPANDED;
            case EXPANDED:
                expandFrame( false );
                break;

            case COLLAPSING:
                mCurrentState = State.COLLAPSED;
            case COLLAPSED:
                collapseFrame( false );
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if( KeyEvent.KEYCODE_BACK == keyCode && this.isOpen() )
        {
            event.startTracking();
            return true;
        }
        return super.onKeyDown( keyCode, event );
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if( KeyEvent.KEYCODE_BACK == keyCode && !event.isCanceled() && this.isOpen() )
        {
            this.collapseFrame();
            return true;
        }
        return super.onKeyUp( keyCode, event );
    }

    public void expandFrame()
    {
        expandFrame( true );
    }

    public void expandFrame( boolean animate )
    {
        mSlidingAnimator.cancel();
        if( animate )
        {
            mCurrentState = State.EXPANDING;
            mSlidingAnimator.setFloatValues( this.getY(), 0.0f );
            mSlidingAnimator.start();
        }
        else
        {
            this.setY( 0.0f );
            mCurrentState = State.EXPANDED;
        }
    }

    public void collapseFrame()
    {
        collapseFrame( true );
    }

    public void collapseFrame( boolean animate )
    {
        mSlidingAnimator.cancel();
        if( animate )
        {
            mCurrentState = State.COLLAPSING;
            mSlidingAnimator.setFloatValues( this.getY(), -this.getHeight() );
            mSlidingAnimator.start();
        }
        else
        {
            this.setY( -this.getHeight() );
            mCurrentState = State.COLLAPSED;
        }
    }

    public void toggleState()
    {
        switch( mCurrentState )
        {
            case EXPANDED:
            case EXPANDING:
                collapseFrame();
                break;

            case COLLAPSED:
            case COLLAPSING:
                expandFrame();
                break;
        }
    }

    public boolean isOpen()
    {
        return mCurrentState == State.EXPANDED || mCurrentState == State.EXPANDING;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        SavedState savedState = new SavedState( super.onSaveInstanceState() );
        savedState.isOpen = isOpen();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState( Parcelable state )
    {
        if( state instanceof SavedState )
        {
            super.onRestoreInstanceState( ( (SavedState) state ).getSuperState() );
            mCurrentState = ( (SavedState) state ).isOpen ? State.EXPANDED : State.COLLAPSED;
        }
        else
        {
            super.onRestoreInstanceState( state );
        }
    }

    public static class SavedState extends BaseSavedState
    {
        boolean isOpen;

        public SavedState( Parcel source )
        {
            super( source );

            isOpen = source.readInt() != 0;
        }

        public SavedState( Parcelable superState )
        {
            super( superState );
        }

        @Override
        public void writeToParcel( @NonNull Parcel destination, int flags )
        {
            super.writeToParcel( destination, flags );
            destination.writeInt( isOpen ? 1 : 0 );
        }

        public static final Creator< SavedState > CREATOR = new Creator< SavedState >()
        {
            @Override
            public SavedState createFromParcel( Parcel source )
            {
                return new SavedState( source );
            }

            @Override
            public SavedState[] newArray( int size )
            {
                return new SavedState[ size ];
            }
        };
    }
}
