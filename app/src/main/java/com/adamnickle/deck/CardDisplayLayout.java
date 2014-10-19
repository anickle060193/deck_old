package com.adamnickle.deck;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.widget.ViewDragHelper;
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
import java.util.LinkedList;


public class CardDisplayLayout extends FrameLayout implements CardHolderListener
{
    private static final long CARD_RECEIVE_VIBRATION = 20L;

    public enum Side
    {
        LEFT, TOP, RIGHT, BOTTOM, NONE
    }

    private ViewDragHelper mDragHelper;
    private GestureDetector mDetector;
    private Vibrator mVibrator;
    private GameUiListener mGameUiListener;

    protected final LinkedList< PlayingCardView > mCardViews;
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

        mDragHelper = ViewDragHelper.create( this, new DragHelperCallback() );
        mDetector = new GestureDetector( getContext(), new CardDisplayGestureListener() );

        mCardViews = new LinkedList< PlayingCardView >();
        mCardViewsByOwner = new HashMap< String, ArrayList< PlayingCardView > >();
        mVibrator = (Vibrator) getContext().getSystemService( Context.VIBRATOR_SERVICE );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View child = getChildAt( i );
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout( lp.Left, lp.Top, lp.Left + child.getMeasuredWidth(), lp.Top + child.getMeasuredHeight() );
        }
    }

    @Override
    protected boolean checkLayoutParams( ViewGroup.LayoutParams p )
    {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        return new LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
    }

    @Override
    protected LayoutParams generateLayoutParams( ViewGroup.LayoutParams p )
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

    public CardHolderListener getCardHolderListener()
    {
        return this;
    }

    public void setGameUiListener( GameUiListener gameUiListener )
    {
        mGameUiListener = gameUiListener;
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

    protected void setGameBackground( int drawableIndex )
    {
        if( drawableIndex == 0 )
        {
            setBackgroundColor( Color.WHITE );
        }
        else
        {
            final TypedArray resources = getResources().obtainTypedArray( R.array.background_drawables );
            final int resource = resources.getResourceId( drawableIndex, -1 );
            BitmapDrawable background = (BitmapDrawable) getResources().getDrawable( resource );
            background.setTileModeXY( Shader.TileMode.REPEAT, Shader.TileMode.REPEAT );
            if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN )
            {
                setBackgroundDrawable( background );
            }
            else
            {
                setBackground( background );
            }
        }
    }

    public void onOrientationChange()
    {
        for( PlayingCardView playingCardView : mCardViews )
        {
            final float temp = playingCardView.getX();
            playingCardView.setX( playingCardView.getY() );
            playingCardView.setY( temp );
        }
    }

    public void childViewOffScreen( PlayingCardView playingCardView )
    {
        if( mGameUiListener != null )
        {
            mGameUiListener.onAttemptSendCard( playingCardView.getOwnerID(), playingCardView.getCard() );
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
        public void onViewPositionChanged( View changedView, int left, int top, int dx, int dy )
        {
            super.onViewPositionChanged( changedView, left, top, dx, dy );

            LayoutParams lp = (LayoutParams) changedView.getLayoutParams();
            lp.Left = left;
            lp.Top = top;
        }

        @Override
        public void onViewReleased( View releasedChild, float xVelocity, float yVelocity )
        {
            ( (PlayingCardView) releasedChild ).fling( xVelocity, yVelocity );
        }
    }

    public class CardDisplayGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            final View view = mDragHelper.findTopChildUnder( (int) event.getX(), (int) event.getY() );
            if( view != null )
            {
                CardDisplayLayout.this.onCardDown( event, (PlayingCardView) view );
            }
            else
            {
                CardDisplayLayout.this.onBackgroundDown( event );
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp( MotionEvent event )
        {
            final View view = mDragHelper.findTopChildUnder( (int) event.getX(), (int) event.getY() );
            if( view != null )
            {
                CardDisplayLayout.this.onCardSingleTap( event, (PlayingCardView) view );
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
            final View view = mDragHelper.findTopChildUnder( (int) event.getX(), (int) event.getY() );
            if( view != null )
            {
                CardDisplayLayout.this.onCardDoubleTap( event, (PlayingCardView) view );
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
            final View view = mDragHelper.findTopChildUnder( (int) event.getX(), (int) event.getY() );
            if( view != null )
            {
                CardDisplayLayout.this.onCardScroll( event, event2, distanceX, distanceY, (PlayingCardView) view );
            }
            else
            {
                CardDisplayLayout.this.onBackgroundScroll( event, event2, distanceX, distanceY );
            }
            return true;
        }

        @Override
        public boolean onFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY )
        {
            final View view = mDragHelper.findTopChildUnder( (int) event.getX(), (int) event.getY() );
            if( view != null )
            {
                CardDisplayLayout.this.onCardFling( event, event2, velocityX, velocityY, (PlayingCardView) view );
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

    public void onCardScroll( MotionEvent event1, MotionEvent event2, float distanceX, float distanceY, PlayingCardView playingCardView )
    {

    }

    public void onBackgroundScroll( MotionEvent event1, MotionEvent event2, float distanceX, float distanceY )
    {

    }

    public void onCardFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY, PlayingCardView playingCardView )
    {

    }

    public void onBackgroundFling( MotionEvent event, MotionEvent event2, float velocityX, float velocityY )
    {

    }

    public void sortCards( String cardHolderID, CardCollection.SortingType sortingType )
    {
        Collections.sort( mCardViews, new PlayingCardView.PlayingCardViewComparator( sortingType ) );
    }

    public synchronized void layoutCards( String cardHolderID )
    {
        if( mCardViews.size() == 0 )
        {
            return;
        }

        final int cardWidth = mCardViews.get( 0 ).getWidth();
        final int cardHeight = mCardViews.get( 0 ).getHeight();
        final int cardHeaderHeight = (int) ( cardHeight * PlayingCardView.CARD_HEADER_PERCENTAGE );

        final int OFFSET = 30;

        final int cardsPerColumn = (int) ( (float) ( this.getHeight() - OFFSET - ( cardHeight - cardHeaderHeight) ) / cardHeaderHeight );
        final int cardsPerRow = this.getWidth() / ( OFFSET + cardWidth );

        if( cardsPerColumn * cardsPerRow < mCardViews.size() )
        {
            DialogHelper.showPopup( getContext(), "Cannot layout cards", "There is not enough room to layout cards...sorry." );
            return;
        }

        int i = 0;
        Iterator<PlayingCardView> playingCardViewIterator = mCardViews.descendingIterator();
        while( playingCardViewIterator.hasNext() )
        {
            final PlayingCardView playingCardView = playingCardViewIterator.next();
            final int xDisplacement = i / cardsPerColumn;
            final int x = OFFSET + cardWidth / 2 + ( xDisplacement ) * ( OFFSET + cardWidth );
            final int yDisplacement = i % cardsPerColumn;
            final int y = OFFSET + cardHeight / 2 + ( yDisplacement ) * ( cardHeaderHeight );
            playingCardView.stop();
            playingCardView.setX( x );
            playingCardView.setY( y );
            i++;
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
    public void onCardRemoved( String playerID, Card card )
    {
        ArrayList<PlayingCardView> cardViews = mCardViewsByOwner.get( playerID );
        if( cardViews != null )
        {
            PlayingCardView removingCardView = null;
            Iterator<PlayingCardView> playingCardViewIterator = cardViews.iterator();
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
                mCardViews.remove( removingCardView );
                this.removeView( removingCardView );
            }
        }
    }

    @Override
    public void onCardsRemoved( String playerID, Card[] cards )
    {
        Arrays.sort( cards, new Card.CardComparator( CardCollection.SortingType.SORT_BY_CARD_NUMBER ) );

        final ArrayList<PlayingCardView> playingCardViews = mCardViewsByOwner.get( playerID );
        final Iterator<PlayingCardView> playingCardViewIterator = playingCardViews.iterator();
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
            this.removeView( cardView );
        }
        mCardViews.removeAll( removedCardViews );
    }

    @Override
    public void onCardAdded( String playerID, Card card )
    {
        final PlayingCardView playingCardView = createPlayingCardView( playerID, card );
        mCardViews.addFirst( playingCardView );
        this.addView( playingCardView );

        ArrayList<PlayingCardView> playingCardViews;
        if( !mCardViewsByOwner.containsKey( playerID ) )
        {
            playingCardViews = new ArrayList< PlayingCardView>();
            mCardViewsByOwner.put( playerID, playingCardViews );
        }
        else
        {
            playingCardViews = mCardViewsByOwner.get( playerID );
        }
        playingCardViews.add( playingCardView );

        mVibrator.vibrate( CARD_RECEIVE_VIBRATION );
    }

    @Override
    public void onCardsAdded( String playerID, Card[] cards )
    {
        ArrayList<PlayingCardView> playingCardViews;
        if( !mCardViewsByOwner.containsKey( playerID ) )
        {
            playingCardViews = new ArrayList< PlayingCardView>();
            mCardViewsByOwner.put( playerID, playingCardViews );
        }
        else
        {
            playingCardViews = mCardViewsByOwner.get( playerID );
        }


        for( Card card : cards )
        {
            final PlayingCardView playingCardView = createPlayingCardView( playerID, card );
            mCardViews.add( playingCardView );
            this.addView( playingCardView );
            playingCardViews.add( playingCardView );
        }

        mVibrator.vibrate( CARD_RECEIVE_VIBRATION );
    }

    @Override
    public void onCardsCleared( String cardHolderID )
    {
        final ArrayList< PlayingCardView > playingCardViews = mCardViewsByOwner.remove( cardHolderID );
        if( playingCardViews != null )
        {
            for( PlayingCardView cardView : playingCardViews )
            {
                this.removeView( cardView );
            }
            mCardViews.removeAll( playingCardViews );
        }
    }
}
