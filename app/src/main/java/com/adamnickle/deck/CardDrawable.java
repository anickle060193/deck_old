package com.adamnickle.deck;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.adamnickle.deck.Game.Card;

public class CardDrawable extends Drawable
{
    private static final String TAG = "CardDrawable";

    public static final int DEFAULT_WIDTH = 598;
    public static final int DEFAULT_HEIGHT = 834;

    private static final float TIME_CONVERT = 1000.0f;
    private static final float DECELERATION_RATE = 0.99f;
    private static final float THRESHOLD_VELOCITY = 45.0f;

    private enum Side { LEFT, TOP, RIGHT, BOTTOM, NONE }

    private View mParent;
    private Card mCard;

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
    private Handler mPositionUpdateHandler;
    private long mLastUpdate;
    private boolean mCanCardsLeave;

    public CardDrawable( final View view, final Resources resources, final Card card, final int x, final int y, final int reqWidth, final int reqHeight, final boolean forceSize )
    {
        mIsBitmapLoaded = false;
        mCard = card;
        mParent = view;
        mX = x;
        mY = y;
        mPositionUpdateHandler = new Handler();
        mCanCardsLeave = true;

        new Thread()
        {
            @Override
            public void run()
            {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource( resources, card.getResource(), options );

                final int width = options.outWidth;
                final int height = options.outHeight;
                int inSampleSize = 1;

                if( height > reqHeight || width > reqWidth )
                {
                    final int halfWidth = width / 2;
                    final int halfHeight = height / 2;

                    while( ( halfHeight / inSampleSize ) > reqHeight
                            && ( halfWidth / inSampleSize ) > reqWidth )
                    {
                        inSampleSize *= 2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource( resources, card.getResource(), options );
                if( forceSize )
                {
                    mWidth = reqWidth;
                    mHeight = reqHeight;
                }
                else
                {
                    mWidth = options.outWidth;
                    mHeight = options.outHeight;
                }

                options.inJustDecodeBounds = false;
                mBitmap = BitmapFactory.decodeResource( resources, card.getResource(), options );

                mDrawRect = new Rect( mX, mY, mX + mWidth, mY + mHeight );
                mIsBitmapLoaded = true;
            }
        }.start();
    }

    public CardDrawable( View view, Resources resources, Card card, int x, int y, int reqWidth, int reqHeight )
    {
        this( view, resources, card, x, y, reqWidth, reqHeight, false );
    }

    public CardDrawable( View view, Resources resources, Card card, int x, int y )
    {
        this( view, resources, card, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT );
    }

    public Card getCard()
    {
        return mCard;
    }

    public void setCanCardsLeave( boolean canCardsLeave )
    {
        mCanCardsLeave = canCardsLeave;
    }

    public boolean canCardsLeave()
    {
        return mCanCardsLeave;
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

    public void setVelocity( float velocityX, float velocityY )
    {
        mVelocityX = velocityX;
        mVelocityY = velocityY;
        mLastUpdate = System.currentTimeMillis();

        mPositionUpdateHandler.postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                updateWithVelocity();

                if( mVelocityX != 0 || mVelocityY != 0 )
                {
                    mPositionUpdateHandler.postDelayed( this, 10 );
                }
            }
        }, 10 );
    }

    private void updateWithVelocity()
    {
        final long now = System.currentTimeMillis();
        final long t = now - mLastUpdate;
        final float dx = mVelocityX * ( t / TIME_CONVERT );
        final float dy = mVelocityY * ( t / TIME_CONVERT );
        mX += (int)dx;
        mY += (int)dy;
        updateBounds();
        mLastUpdate = now;

        if( canCardsLeave() )
        {
            Side side = hasHitWall();
            switch( side )
            {
                case LEFT:
                    final int leftDiff = mParent.getLeft() - mDrawRect.left + 1;
                    update( mX + leftDiff, mY );
                    mVelocityX = -mVelocityX;
                    break;
                case RIGHT:
                    final int rightDiff = mDrawRect.right - mParent.getRight() + 1;
                    update( mX - rightDiff, mY );
                    mVelocityX = -mVelocityX;
                    break;
                case TOP:
                    final int topDiff = mParent.getTop() - mDrawRect.top + 1;
                    update( mX, mY + topDiff );
                    mVelocityY = -mVelocityY;
                    break;
                case BOTTOM:
                    final int bottomDiff = mDrawRect.bottom - mParent.getBottom() + 1;
                    update( mX, mY - bottomDiff );
                    mVelocityY = -mVelocityY;
                    break;
            }
        }
        else
        {
            if( !isOnScreen() )
            {
                mVelocityX = 0.0f;
                mVelocityY = 0.0f;
            }
        }

        decelerateCard();
    }

    private void decelerateCard()
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
        } else
        {
            mVelocityX = 0.0f;
            mVelocityY = 0.0f;
        }
    }

    private Side hasHitWall()
    {
        if( mDrawRect.left <= mParent.getLeft() )
        {
            return Side.LEFT;
        }
        else if( mDrawRect.top < mParent.getTop() )
        {
            return Side.TOP;
        }
        else if( mDrawRect.right > mParent.getRight() )
        {
            return Side.RIGHT;
        }
        else if( mDrawRect.bottom > mParent.getBottom() )
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
        mParent.getDrawingRect( parentRect );
        return Rect.intersects( parentRect, mDrawRect );
    }

    @SuppressWarnings( "SuspiciousNameCombination" )
    public void onOrientationChange()
    {
        final int newTop = mDrawRect.left;
        final int newLeft = mDrawRect.top;
        mDrawRect.set( newLeft, newTop, newLeft + mWidth, newTop + mHeight );
    }

    private void updateBounds()
    {
        if( !mIsBitmapLoaded ) return;

        mDrawRect.offsetTo( (int)( mX - mWidth / 2.0f ), (int)( mY - mHeight / 2.0f ) );
    }

    public void update( int x, int y )
    {
        if( !mIsBitmapLoaded ) return;

        mX = x;
        mY = y;
        updateBounds();
    }

    public boolean contains( int x, int y )
    {
        return mDrawRect.contains( x, y );
    }

    @Override
    public void draw( Canvas canvas )
    {
        if( !mIsBitmapLoaded ) return;

        canvas.drawBitmap( mBitmap, null, mDrawRect, null );
    }

    @Override
    public void setAlpha( int newAlpha )
    {
    }

    @Override
    public void setColorFilter( ColorFilter colorFilter )
    {
    }

    @Override
    public int getOpacity()
    {
        return 1;
    }
}
