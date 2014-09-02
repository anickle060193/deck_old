package com.adamnickle.deck;

import android.app.Activity;
import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.spi.GameUiInterface;
import com.adamnickle.deck.spi.GameUiListener;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Displays game related items such as cards, points, etc.
 */
public class GameView extends View implements GameUiInterface
{
    /**
     * Log TAG for {@link com.adamnickle.deck.GameView}.
     */
    private static final String TAG = "GameView";

    private static final int CARD_WIDTH = (int)( CardDrawable.DEFAULT_WIDTH / 2.0f );
    private static final int CARD_HEIGHT = (int)( CardDrawable.DEFAULT_HEIGHT / 2.0f );

    /**
     * The minimum velocity required for a series of {@link android.view.MotionEvent}s to be considered a Fling gesture.
     */
    private static final float MINIMUM_VELOCITY = 400.0f;

    /**
     * The {@link android.support.v4.view.GestureDetectorCompat} to process the MotionEvents for this view.
     */
    private GestureDetectorCompat mDetector;
    /**
     * A {@link java.util.LinkedList} of {@link com.adamnickle.deck.CardDrawable}s that represent all cards currently being drawn on the {@link com.adamnickle.deck.GameView}.
     * The {@link com.adamnickle.deck.CardDrawable}s are drawn in reverse order, such that the first {@link com.adamnickle.deck.CardDrawable} in the list will
     * be drawn on top and the last {@link com.adamnickle.deck.CardDrawable} will be drawn underneath all others.
     */
    private final LinkedList<CardDrawable> mCardDrawables;
    /**
     * A {@link android.util.SparseArray} of {@link com.adamnickle.deck.CardDrawable}s that are currently
     * being moved by the user.
     * The {@link android.util.SparseArray} is setup such that the index for a specific {@link com.adamnickle.deck.CardDrawable}
     * is the ID of the pointer currently moving the {@link com.adamnickle.deck.CardDrawable}.
     */
    private SparseArray<CardDrawable> mMovingCardDrawables;

    private final Activity mParentActivity;
    private GameUiListener mListener;

    /**
     * Constructs a {@link com.adamnickle.deck.GameView} object with the given {@link com.adamnickle.deck.GameActivity} as the {@link android.view.View}'s context.
     * @param activity A {@link com.adamnickle.deck.GameActivity} that will display the constructed {@link com.adamnickle.deck.GameView}.
     */
    public GameView( Activity activity )
    {
        super( activity );
        Log.d( TAG, "___ CONSTRUCTOR ___" );

        mParentActivity = activity;
        mDetector = new GestureDetectorCompat( activity, mGestureListener );
        mCardDrawables = new LinkedList< CardDrawable >();
        mMovingCardDrawables = new SparseArray< CardDrawable >();
    }

    public void setGameUiListener( GameUiListener gameUiListener )
    {
        mListener = gameUiListener;
    }

    /**
     * Called when {@link com.adamnickle.deck.GameView} is attached to the {@link com.adamnickle.deck.GameActivity}'s {@link android.view.Window}.
     */
    @Override
    protected void onAttachedToWindow()
    {
        postDelayed( mUpdateScreen, 10 );
    }

    /**
     * A {@link java.lang.Runnable} that continuously runs to re-draw the {@link com.adamnickle.deck.GameView}.
     */
    private Runnable mUpdateScreen = new Runnable()
    {
        @Override
        public void run()
        {
            GameView.this.invalidate();

            postDelayed( this, 10 );
        }
    };

    /**
     * Draws the current state of the {@link com.adamnickle.deck.GameView} to the screen.
     * @param canvas The {@link android.graphics.Canvas} to draw to.
     */
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

    /**
     * Handles a {@link android.view.MotionEvent} corresponding to a user touch input for this {@link com.adamnickle.deck.GameView}.
     * @param event The generated {@link android.view.MotionEvent}.
     * @return Returns whether to continue tracking {@link android.view.MotionEvent}s related to the given {@link android.view.MotionEvent}.
     */
    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        mDetector.onTouchEvent( event );

        final int action = MotionEventCompat.getActionMasked( event );
        switch( action )
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                switch( action )
                {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                }
                final int pointerIndex = MotionEventCompat.getActionIndex( event );
                final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );
                final float x = MotionEventCompat.getX( event, pointerIndex );
                final float y = MotionEventCompat.getY( event, pointerIndex );

                CardDrawable activeCardDrawable = null;
                for( CardDrawable cardDrawable : mCardDrawables )
                {
                    if( cardDrawable != null && !cardDrawable.isHeld() && cardDrawable.contains( (int) x, (int) y ) )
                    {
                        activeCardDrawable = cardDrawable;
                        break;
                    }
                }
                if( activeCardDrawable != null )
                {
                    activeCardDrawable.setIsHeld( true );
                    mMovingCardDrawables.put( pointerId, activeCardDrawable );
                    mCardDrawables.removeFirstOccurrence( activeCardDrawable );
                    mCardDrawables.addFirst( activeCardDrawable );
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
                Log.d( TAG, "--- ACTION CANCEL ---" );
                for( int i = 0; i < mMovingCardDrawables.size(); i++ )
                {
                    mMovingCardDrawables.valueAt( i ).setIsHeld( false );
                }
                mMovingCardDrawables.clear();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            {
                switch( action )
                {
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                }
                final int pointerIndex = MotionEventCompat.getActionIndex( event );
                final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );
                final CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                if( cardDrawable != null )
                {
                    cardDrawable.setIsHeld( false );
                    mMovingCardDrawables.remove( pointerId );
                }
                break;
            }
        }

        return true;
    }

    /**
     * Handles a screen orientation change.
     */
    public void onOrientationChange()
    {
        Log.d( TAG, "__ ORIENTATION CHANGE __" );
        for( CardDrawable cardDrawable : mCardDrawables )
        {
            cardDrawable.onOrientationChange();
        }
    }

    /**
     * The {@link android.view.GestureDetector.SimpleOnGestureListener} that receives gesture callbacks.
     */
    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener()
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            return true;
        }

        @Override
        public boolean onFling( MotionEvent event1, MotionEvent event2, float velocityX, float velocityY )
        {
            final float velocity = (float)Math.sqrt( velocityX * velocityX + velocityY * velocityY );
            if( velocity > MINIMUM_VELOCITY )
            {
                final int pointerIndex = MotionEventCompat.getActionIndex( event2 );
                final int pointerId = MotionEventCompat.getPointerId( event2, pointerIndex );
                final CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                if( cardDrawable != null )
                {
                    cardDrawable.setVelocity( velocityX, velocityY );
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap( MotionEvent event )
        {
            final int pointerIndex = MotionEventCompat.getActionIndex( event );
            final int pointerId = MotionEventCompat.getPointerId( event, pointerIndex );
            final CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
            if( mListener.onAttemptSendCard( cardDrawable.getCard() ) )
            {
                mCardDrawables.remove( cardDrawable );
            }
            return true;
        }
    };

    /*******************************************************************
     * GameUiInterface Methods
     *******************************************************************/
    @Override
    public void addCard( Card card )
    {
        mCardDrawables.addFirst( new CardDrawable( this, getResources(), card, getWidth() / 2, getHeight() / 2, CARD_WIDTH, CARD_HEIGHT, true ) );
    }

    @Override
    public boolean removeCard( Card card )
    {
        for( CardDrawable cardDrawable : mCardDrawables )
        {
            if( cardDrawable.getCard().equals( card ) )
            {
                mCardDrawables.remove( cardDrawable );
                return true;
            }
        }
        return false;
    }

    @Override
    public void displayNotification( final String notification )
    {
        mParentActivity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText( mParentActivity, notification, Toast.LENGTH_SHORT ).show();
            }
        } );
    }
}
