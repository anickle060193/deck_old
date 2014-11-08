package com.adamnickle.deck;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Interfaces.CardHolderListener;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


public class CardDisplayLayout extends FrameLayout implements CardHolderListener
{
    private static final long CARD_RECEIVE_VIBRATION = 40L;

    public enum Side
    {
        LEFT, TOP, RIGHT, BOTTOM, NONE
    }

    private final GestureDetector mDetector;
    private final Vibrator mVibrator;
    private GameUiListener mGameUiListener;

    protected final HashMap< String, ArrayList< PlayingCardView > > mCardViewsByOwner;

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

        mDetector = new GestureDetector( getContext(), new CardDisplayGestureListener() );

        mCardViewsByOwner = new HashMap< String, ArrayList< PlayingCardView > >();
        mVibrator = (Vibrator) getContext().getSystemService( Context.VIBRATOR_SERVICE );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );

        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View child = getChildAt( i );
            if( child instanceof PlayingCardView )
            {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.layout( lp.Left, lp.Top, lp.Left + child.getMeasuredWidth(), lp.Top + child.getMeasuredHeight() );
            }
        }
    }

    @Override
    protected boolean checkLayoutParams( ViewGroup.LayoutParams p )
    {
        return ( p instanceof LayoutParams );
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        return new LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
    }

    @Override
    protected LayoutParams generateLayoutParams( @NonNull ViewGroup.LayoutParams p )
    {
        return new LayoutParams( p.width, p.height );
    }

    public static class LayoutParams extends FrameLayout.LayoutParams
    {
        public int Left;
        public int Top;

        public LayoutParams( int width, int height )
        {
            super( width, height );
        }
    }

    public void onViewDestroy()
    {
        for( ArrayList< PlayingCardView > views : mCardViewsByOwner.values() )
        {
            views.clear();
        }
        mCardViewsByOwner.clear();
        this.removeAllViews();
        System.gc();
    }

    public CardHolderListener getCardHolderListener()
    {
        return this;
    }

    public void setGameUiListener( GameUiListener gameUiListener )
    {
        mGameUiListener = gameUiListener;
    }

    @Override
    public void setBackgroundResource( int resourceID )
    {
        super.setBackgroundResource( resourceID );
        final Drawable background = this.getBackground();
        if( background instanceof BitmapDrawable )
        {
            ( (BitmapDrawable) background ).setTileModeXY( Shader.TileMode.REPEAT, Shader.TileMode.REPEAT );
        }
    }

    @SuppressWarnings( "SuspiciousNameCombination" )
    public void onOrientationChange()
    {
        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View view = getChildAt( i );
            if( view instanceof PlayingCardView )
            {
                final float x = view.getX();
                view.setX( view.getY() );
                view.setY( x );
            }
        }
    }

    public void childViewOffScreen( PlayingCardView playingCardView, Side side )
    {
        if( mGameUiListener != null )
        {
            mGameUiListener.onAttemptSendCard( playingCardView.getOwnerID(), playingCardView.getCard(), side );
        }
    }

    public boolean childShouldBounce( PlayingCardView playingCardView, Side wall )
    {
        if( mGameUiListener != null )
        {
            return !mGameUiListener.canSendCard( playingCardView.getOwnerID(), playingCardView.getCard() );
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent( MotionEvent event )
    {
        return true;
    }

    private HashMap< Integer, PlayingCardView > mMovingCards = new HashMap< Integer, PlayingCardView >();
    private HashMap< Integer, PointF > mLastPoints = new HashMap< Integer, PointF >();
    private MotionEvent mCurrentDownEvent;

    @Override
    public boolean onTouchEvent( @NonNull MotionEvent event )
    {
        final int action = event.getActionMasked();
        final int pointerIndex = event.getActionIndex();
        final int pointerId = event.getPointerId( pointerIndex );
        final float x = event.getX( pointerIndex );
        final float y = event.getY( pointerIndex );

        switch( action )
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                mCurrentDownEvent = event;
                PlayingCardView playingCardView = findFirstCardUnder( x, y );
                if( mMovingCards.containsValue( playingCardView ) )
                {
                    return true;
                }
                mLastPoints.put( pointerId, new PointF( x, y ) );
                if( playingCardView != null )
                {
                    CardDisplayLayout.this.onCardDown( event, playingCardView );
                    mMovingCards.put( pointerId, playingCardView );
                }
                else
                {
                    CardDisplayLayout.this.onBackgroundDown( event );
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:
            {
                for( int i = 0; i < event.getPointerCount(); i++ )
                {
                    final int movePointerID = event.getPointerId( i );
                    final PointF lastPoint = mLastPoints.get( movePointerID );
                    if( lastPoint != null )
                    {
                        final float moveX = event.getX( i );
                        final float moveY = event.getY( i );
                        final float deltaX = lastPoint.x - moveX;
                        final float deltaY = lastPoint.y - moveY;
                        final PlayingCardView playingCardView = mMovingCards.get( movePointerID );
                        if( playingCardView != null )
                        {
                            CardDisplayLayout.this.onCardScroll( mCurrentDownEvent, event, deltaX, deltaY, playingCardView );
                        }
                        else
                        {
                            CardDisplayLayout.this.onBackgroundScroll( mCurrentDownEvent, event, deltaX, deltaY );
                        }

                        lastPoint.set( moveX, moveY );
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            {
                mMovingCards.remove( pointerId );
                mLastPoints.remove( pointerId );
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            {
                mMovingCards.clear();
                mLastPoints.clear();
                break;
            }
        }

        mDetector.onTouchEvent( event );
        return true;
    }

    public PlayingCardView findFirstCardUnder( float x, float y )
    {
        final int childCount = getChildCount();
        for( int i = childCount - 1; i >= 0; i-- )
        {
            final View child = getChildAt( i );
            if( child instanceof PlayingCardView )
            {
                if( ( (PlayingCardView) child ).contains( x, y ) )
                {
                    return (PlayingCardView) child;
                }
            }
        }
        return null;
    }

    public class CardDisplayGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            return true;
        }

        @Override
        public boolean onSingleTapUp( MotionEvent event )
        {
            final int pointerIndex = event.getActionIndex();
            final PlayingCardView playingCardView = findFirstCardUnder( event.getX( pointerIndex ), event.getY( pointerIndex ) );
            if( playingCardView != null )
            {
                CardDisplayLayout.this.onCardSingleTap( event, playingCardView );
            }
            else
            {
                CardDisplayLayout.this.onBackgroundSingleTap( event );
            }
            return true;
        }

        @Override
        public boolean onDoubleTap( MotionEvent event )
        {
            final int pointerIndex = event.getActionIndex();
            final PlayingCardView playingCardView = findFirstCardUnder( event.getX( pointerIndex ), event.getY( pointerIndex ) );
            if( playingCardView != null )
            {
                CardDisplayLayout.this.onCardDoubleTap( event, playingCardView );
            }
            else
            {
                CardDisplayLayout.this.onBackgroundDoubleTap( event );
            }
            return true;
        }

        @Override
        public boolean onScroll( MotionEvent event, MotionEvent event2, float distanceX, float distanceY )
        {
            return true;
        }

        @Override
        public boolean onFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY )
        {
            final int pointerIndex = event.getActionIndex();
            final PlayingCardView playingCardView = findFirstCardUnder( event2.getX( pointerIndex ), event2.getY( pointerIndex ) );
            if( playingCardView != null )
            {
                CardDisplayLayout.this.onCardFling( event, event2, velocityX, velocityY, playingCardView );
            }
            else
            {
                CardDisplayLayout.this.onBackgroundFling( event, event2, velocityX, velocityY );
            }
            return true;
        }
    }

    public void onBackgroundDown( MotionEvent event )
    {
    }

    public void onCardDown( MotionEvent event, PlayingCardView playingCardView )
    {
        playingCardView.bringToFront();
        playingCardView.onTouched();
    }

    public void onBackgroundSingleTap( MotionEvent event )
    {
    }

    public void onCardSingleTap( MotionEvent event, PlayingCardView playingCardView )
    {
    }

    public void onBackgroundDoubleTap( MotionEvent event )
    {
    }

    public void onCardDoubleTap( MotionEvent event, PlayingCardView playingCardView )
    {
    }

    public void onCardScroll( MotionEvent initialDownEvent, MotionEvent event, float deltaX, float deltaY, PlayingCardView playingCardView )
    {
        playingCardView.setX( playingCardView.getX() - deltaX );
        playingCardView.setY( playingCardView.getY() - deltaY );
    }

    public void onBackgroundScroll( MotionEvent event1, MotionEvent event2, float distanceX, float distanceY )
    {
    }

    public void onCardFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY, PlayingCardView playingCardView )
    {
        playingCardView.fling( velocityX, velocityY );
    }

    public void onBackgroundFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY )
    {
    }

    public void sortCards( String cardHolderID, CardCollection.SortingType sortingType )
    {
        final ArrayList< PlayingCardView > playingCardViews = new ArrayList< PlayingCardView >();
        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View child = getChildAt( i );
            if( child instanceof PlayingCardView )
            {
                playingCardViews.add( (PlayingCardView) child );
            }
        }
        Collections.sort( playingCardViews, new PlayingCardView.PlayingCardViewComparator( sortingType ) );
        for( PlayingCardView playingCardView : playingCardViews )
        {
            playingCardView.bringToFront();
        }
    }

    public synchronized void layoutCards( String cardHolderID )
    {
        if( getChildCount() == 0 )
        {
            return;
        }

        int x = 50;
        int y = 50;
        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View child = getChildAt( i );
            if( child instanceof PlayingCardView )
            {
                final PlayingCardView playingCardView = (PlayingCardView) child;
                playingCardView.stop();
                playingCardView.flip( true, false );
                playingCardView.setX( x );
                playingCardView.setY( y );
                x += (int) ( playingCardView.getWidth() * 0.30f );
                if( x + playingCardView.getWidth() > this.getWidth() )
                {
                    x = 50;
                    y += playingCardView.getHeight() + 10;
                }
            }
        }
    }

    public void resetCard( String cardHolderID, Card card )
    {
        for( PlayingCardView playingCardView : mCardViewsByOwner.get( cardHolderID ) )
        {
            if( playingCardView.getCard().equals( card ) )
            {
                playingCardView.reset();
                break;
            }
        }
    }

    public PlayingCardView createPlayingCardView( String cardHolderID, Card card )
    {
        return new PlayingCardView( getContext(), cardHolderID, card );
    }

    @Override
    public void onCardRemoved( final String playerID, final Card card )
    {
        this.post( new Runnable()
        {
            @Override
            public void run()
            {
                ArrayList< PlayingCardView > cardViews = mCardViewsByOwner.get( playerID );
                if( cardViews != null )
                {
                    PlayingCardView removingCardView = null;
                    Iterator< PlayingCardView > playingCardViewIterator = cardViews.iterator();
                    while( playingCardViewIterator.hasNext() )
                    {
                        PlayingCardView playingCardView = playingCardViewIterator.next();
                        if( playingCardView.getCard().equals( card ) )
                        {
                            removingCardView = playingCardView;
                            playingCardViewIterator.remove();
                            break;
                        }
                    }
                    if( removingCardView != null )
                    {
                        CardDisplayLayout.this.removeView( removingCardView );
                    }
                }
            }
        } );
    }

    @Override
    public void onCardsRemoved( final String playerID, final Card[] cards )
    {
        this.post( new Runnable()
        {
            @Override
            public void run()
            {
                Arrays.sort( cards, new Card.CardComparator( CardCollection.SortingType.SORT_BY_CARD_NUMBER ) );

                final ArrayList< PlayingCardView > playingCardViews = mCardViewsByOwner.get( playerID );
                final Iterator< PlayingCardView > playingCardViewIterator = playingCardViews.iterator();
                final ArrayList< PlayingCardView > removedCardViews = new ArrayList< PlayingCardView >();

                while( playingCardViewIterator.hasNext() && ( removedCardViews.size() < cards.length ) )
                {
                    final PlayingCardView playingCardView = playingCardViewIterator.next();
                    if( Arrays.binarySearch( cards, playingCardView.getCard() ) >= 0 )
                    {
                        removedCardViews.add( playingCardView );
                        playingCardViewIterator.remove();
                    }
                }
                for( PlayingCardView cardView : removedCardViews )
                {
                    CardDisplayLayout.this.removeView( cardView );
                }
            }
        } );
    }

    @Override
    public void onCardAdded( final String playerID, final Card card )
    {
        this.post( new Runnable()
        {
            @Override
            public void run()
            {
                final PlayingCardView playingCardView = createPlayingCardView( playerID, card );
                CardDisplayLayout.this.addView( playingCardView );

                ArrayList< PlayingCardView > playingCardViews;
                if( !mCardViewsByOwner.containsKey( playerID ) )
                {
                    playingCardViews = new ArrayList< PlayingCardView >();
                    mCardViewsByOwner.put( playerID, playingCardViews );
                }
                else
                {
                    playingCardViews = mCardViewsByOwner.get( playerID );
                }
                playingCardViews.add( playingCardView );

                mVibrator.vibrate( CARD_RECEIVE_VIBRATION );
            }
        } );
    }

    @Override
    public void onCardsAdded( final String playerID, final Card[] cards )
    {
        this.post( new Runnable()
        {
            @Override
            public void run()
            {
                final ArrayList< PlayingCardView > playingCardViews;
                if( !mCardViewsByOwner.containsKey( playerID ) )
                {
                    playingCardViews = new ArrayList< PlayingCardView >();
                    mCardViewsByOwner.put( playerID, playingCardViews );
                }
                else
                {
                    playingCardViews = mCardViewsByOwner.get( playerID );
                }

                System.gc();
                for( Card card : cards )
                {
                    final PlayingCardView playingCardView = createPlayingCardView( playerID, card );
                    CardDisplayLayout.this.addView( playingCardView );
                    playingCardViews.add( playingCardView );
                }

                mVibrator.vibrate( CARD_RECEIVE_VIBRATION );
            }
        } );
    }

    @Override
    public void onCardsCleared( final String cardHolderID )
    {
        this.post( new Runnable()
        {
            @Override
            public void run()
            {
                ArrayList< PlayingCardView > playingCardViews = mCardViewsByOwner.remove( cardHolderID );
                if( playingCardViews != null )
                {
                    for( PlayingCardView cardView : playingCardViews )
                    {
                        CardDisplayLayout.this.removeView( cardView );
                    }
                    playingCardViews.clear();
                    System.gc();
                }
            }
        } );
    }
}
