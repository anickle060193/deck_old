package com.adamnickle.deck;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.Deck;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.adamnickle.deck.Interfaces.GameUiView;

import java.util.Comparator;

public class CardDrawable
{
    public static final float CARD_HEADER_PERCENTAGE = 0.27f;

    private static final int ORIGINAL_OFFSET = 200;

    private static final float MILLISECONDS_TO_SECONDS = 1.0f / 1000.0f;
    private static final float DECELERATION_RATE = 0.99f;
    private static final float THRESHOLD_VELOCITY = 45.0f;

    private static boolean mAreBackBitmapsLoaded = false;
    private static boolean mStartedLoadingBacks = false;
    private static Bitmap mBlueBack;
    private static Bitmap mRedBack;

    private enum Side
    {
        LEFT, TOP, RIGHT, BOTTOM, NONE
    }

    private final GameUiView mGameUiView;
    private final GameUiListener mListener;
    private final Card mCard;

    private String mOwnerID;
    private Bitmap mBitmap;
    private Rect mDrawRect;
    private int mWidth;
    private int mHeight;
    private int mX;
    private int mY;
    private boolean mIsBitmapLoaded;
    private boolean mIsHeld;
    private float mVelocityX;
    private float mVelocityY;
    private Runnable mPositionUpdateRunnable;
    private long mLastUpdate;
    private boolean mIsFaceUp;

    public CardDrawable( GameUiView parentView, GameUiListener gameUiListener, String ownerID, Card card, final float scalingFactor )
    {
        mIsBitmapLoaded = false;
        mOwnerID = ownerID;
        mCard = card;
        mGameUiView = parentView;
        mListener = gameUiListener;
        mX = 0;
        mY = 0;
        mIsFaceUp = false;

        mPositionUpdateRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateWithVelocity();

                if( isMoving() && isOnScreen() )
                {
                    mGameUiView.postDelayed( this, 10 );
                }
            }
        };

        new Thread()
        {
            @Override
            public void run()
            {
                mBitmap = BitmapFactory.decodeResource( mGameUiView.getResources(), mCard.getResource(), null );
                mWidth = (int) ( mBitmap.getWidth() * scalingFactor );
                mHeight = (int) ( mBitmap.getHeight() * scalingFactor );

                mX -= mWidth / 2.0f;
                mY -= mHeight / 2.0f;

                mDrawRect = new Rect( mX, mY, mX + mWidth, mY + mHeight );
                mIsBitmapLoaded = true;
            }
        }.start();

        synchronized( CardDrawable.class )
        {
            if( !mStartedLoadingBacks )
            {
                mStartedLoadingBacks = true;
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        final Resources resources = mGameUiView.getResources();
                        mBlueBack = BitmapFactory.decodeResource( resources, CardResources.BLUE_CARD_BACK, null );
                        mRedBack = BitmapFactory.decodeResource( resources, CardResources.RED_CARD_BACK, null );
                        mAreBackBitmapsLoaded = true;
                    }
                }.start();
            }
        }

        resetCardDrawable();
    }

    public CardDrawable( GameUiView parentView, GameUiListener gameUiListener, String ownerID, Card card )
    {
        this( parentView, gameUiListener, ownerID, card, 1.0f );
    }

    public void draw( Canvas canvas )
    {
        if( mIsBitmapLoaded )
        {
            if( mIsFaceUp )
            {
                canvas.drawBitmap( mBitmap, null, mDrawRect, null );
            }
            else
            {
                if( mAreBackBitmapsLoaded )
                {
                    canvas.drawBitmap( mBlueBack, null, mDrawRect, null );
                }
            }
        }
    }

    public String getOwnerID()
    {
        return mOwnerID;
    }

    public Card getCard()
    {
        return mCard;
    }

    public int getWidth()
    {
        return mWidth;
    }

    public int getHeight()
    {
        return mHeight;
    }

    public boolean isHeld()
    {
        return mIsHeld;
    }

    public void setIsHeld( boolean isHeld )
    {
        mIsHeld = isHeld;
        if( mIsHeld )
        {
            mVelocityX = 0.0f;
            mVelocityY = 0.0f;
        }
    }

    public boolean isMoving()
    {
        return mVelocityX != 0 || mVelocityY != 0;
    }

    public void flipFaceUp()
    {
        mIsFaceUp = true;
    }

    public synchronized void setVelocity( float velocityX, float velocityY )
    {
        mVelocityX = velocityX;
        mVelocityY = velocityY;
        mLastUpdate = System.currentTimeMillis();

        new Thread( mPositionUpdateRunnable ).start();
    }

    public synchronized void resetCardDrawable()
    {
        mX = (int) ( mGameUiView.getWidth() / 2.0f + Deck.RANDOM.nextInt( ORIGINAL_OFFSET ) - ORIGINAL_OFFSET / 2.0f );
        mY = (int) ( mGameUiView.getHeight() / 2.0f + Deck.RANDOM.nextInt( ORIGINAL_OFFSET ) - ORIGINAL_OFFSET / 2.0f );
        updateBounds();
        mVelocityX = 0.0f;
        mVelocityY = 0.0f;
    }

    private synchronized void updateWithVelocity()
    {
        final long now = System.currentTimeMillis();
        final long t = now - mLastUpdate;
        final float dx = mVelocityX * ( t * MILLISECONDS_TO_SECONDS );
        final float dy = mVelocityY * ( t * MILLISECONDS_TO_SECONDS );
        mX += (int) dx;
        mY += (int) dy;
        updateBounds();
        mLastUpdate = now;

        if( !mListener.canSendCard( mOwnerID, this.getCard() ) )
        {
            switch( hasHitWall() )
            {
                case LEFT:
                    final int leftDiff = mGameUiView.getLeft() - mDrawRect.left + 1;
                    update( mX + leftDiff, mY );
                    mVelocityX = -mVelocityX;
                    break;
                case RIGHT:
                    final int rightDiff = mDrawRect.right - mGameUiView.getRight() + 1;
                    update( mX - rightDiff, mY );
                    mVelocityX = -mVelocityX;
                    break;
                case TOP:
                    final int topDiff = mGameUiView.getTop() - mDrawRect.top + 1;
                    update( mX, mY + topDiff );
                    mVelocityY = -mVelocityY;
                    break;
                case BOTTOM:
                    final int bottomDiff = mDrawRect.bottom - mGameUiView.getBottom() + 1;
                    update( mX, mY - bottomDiff );
                    mVelocityY = -mVelocityY;
                    break;
            }
        }
        else
        {
            if( !isOnScreen() )
            {
                if( !mListener.onAttemptSendCard( mOwnerID, getCard() ) )
                {
                    resetCardDrawable();
                }
                return;
            }
        }

        decelerateCard();
    }

    private synchronized void decelerateCard()
    {
        final float newVelocity = DECELERATION_RATE * (float) Math.sqrt( mVelocityX * mVelocityX + mVelocityY * mVelocityY );
        final int xSign = mVelocityX > 0 ? 1 : -1;
        final int ySign = mVelocityY > 0 ? 1 : -1;
        if( newVelocity > THRESHOLD_VELOCITY )
        {
            final float theta = (float) Math.atan( mVelocityY / mVelocityX );
            mVelocityX = (float) ( newVelocity * Math.cos( theta ) );
            mVelocityX = xSign * Math.abs( mVelocityX );
            mVelocityY = (float) ( newVelocity * Math.sin( theta ) );
            mVelocityY = ySign * Math.abs( mVelocityY );
        }
        else
        {
            mVelocityX = 0.0f;
            mVelocityY = 0.0f;
        }
    }

    private Side hasHitWall()
    {
        if( mDrawRect.left <= mGameUiView.getLeft() )
        {
            return Side.LEFT;
        }
        else if( mDrawRect.top <= mGameUiView.getTop() )
        {
            return Side.TOP;
        }
        else if( mDrawRect.right >= mGameUiView.getRight() )
        {
            return Side.RIGHT;
        }
        else if( mDrawRect.bottom >= mGameUiView.getBottom() )
        {
            return Side.BOTTOM;
        }
        else
        {
            return Side.NONE;
        }
    }

    private boolean isOnScreen()
    {
        final Rect parentRect = new Rect();
        mGameUiView.getDrawingRect( parentRect );
        return Rect.intersects( parentRect, mDrawRect );
    }

    public void onOrientationChange()
    {
        final int newTop = mDrawRect.left;
        final int newLeft = mDrawRect.top;
        mDrawRect.set( newLeft, newTop, newLeft + mWidth, newTop + mHeight );
    }

    private synchronized void updateBounds()
    {
        if( !mIsBitmapLoaded )
        {
            return;
        }

        mDrawRect.offsetTo( (int) ( mX - mWidth / 2.0f ), (int) ( mY - mHeight / 2.0f ) );
    }

    public synchronized void update( int x, int y )
    {
        if( !mIsBitmapLoaded )
        {
            return;
        }

        mX = Math.min( Math.max( x, 0 ), mGameUiView.getWidth() );
        mY = Math.min( Math.max( y, 0 ), mGameUiView.getHeight() );

        updateBounds();
    }

    public boolean contains( int x, int y )
    {
        return mDrawRect.contains( x, y );
    }

    public static class CardDrawableComparator implements Comparator< CardDrawable >
    {
        private final Card.CardComparator mCardComparator;

        public CardDrawableComparator( CardCollection.SortingType sortType )
        {
            mCardComparator = new Card.CardComparator( sortType );
        }

        @Override
        public int compare( CardDrawable cardDrawable, CardDrawable cardDrawable2 )
        {
            return mCardComparator.compare( cardDrawable.getCard(), cardDrawable2.getCard() );
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        mBitmap.recycle();

        super.finalize();
    }
}