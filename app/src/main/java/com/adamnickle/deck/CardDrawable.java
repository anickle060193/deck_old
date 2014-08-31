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

/**
 * Displays a visual representation of a playing card.
 */
public class CardDrawable extends Drawable
{
    /**
     * Log TAG for {@link com.adamnickle.deck.CardDrawable}.
     */
    private static final String TAG = "CardDrawable";

    /**
     * Default {@link com.adamnickle.deck.CardDrawable} drawn width
     */
    public static final int DEFAULT_WIDTH = 598;
    /**
     * Default {@link com.adamnickle.deck.CardDrawable} drawn height
     */
    public static final int DEFAULT_HEIGHT = 834;

    /**
     * Conversion factor between seconds and milliseconds
     */
    private static final float TIME_CONVERT = 1000.0f;

    /**
     * The rate at which the moving {@link com.adamnickle.deck.CardDrawable} will slow down.
     */
    private static final float DECELERATION_RATE = 0.99f;

    /**
     * The minimum allowed velocity.
     */
    private static final float THRESHOLD_VELOCITY = 45.0f;

    /**
     * A {@link java.lang.Enum} of screen sides
     */
    private enum Side
    {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        NONE
    }

    /**
     * The  {@link android.view.View} that this {@link com.adamnickle.deck.CardDrawable} will be
     * drawn to
     */
    private View mParent;
    private Card mCard;

    /**
     * The {@link android.graphics.Bitmap} of the card this
     * {@link com.adamnickle.deck.CardDrawable} corresponds to
     */
    private Bitmap mBitmap;
    /**
     * The region of the screen this {@link com.adamnickle.deck.CardDrawable} will be drawn to
     */
    private Rect mDrawRect;
    /**
     * The width of this {@link com.adamnickle.deck.CardDrawable}
     */
    private int mWidth;
    /**
     * The height of this {@link com.adamnickle.deck.CardDrawable}
     */
    private int mHeight;
    /**
     * The X coordinate of the location of the center of this
     * {@link com.adamnickle.deck.CardDrawable}
     */
    private int mX;
    /**
     * The Y coordinate of the location of the center of this
     * {@link com.adamnickle.deck.CardDrawable}
     */
    private int mY;
    /**
     * A flag for if {@link com.adamnickle.deck.CardDrawable#mBitmap} has been loaded or not
     */
    private boolean mIsBitmapLoaded;
    /**
     * A flag for if the user is currently touching the drawn image representing this
     * {@link com.adamnickle.deck.CardDrawable} on the screen
     */
    private boolean mIsHeld;
    /**
     * The X component of the velocity for this {@link com.adamnickle.deck.CardDrawable}
     */
    private float mVelocityX;
    /**
     * The Y component of the velocity for this {@link com.adamnickle.deck.CardDrawable}
     */
    private float mVelocityY;
    /**
     * The {@link android.os.Handler} responsible for updating the position of this
     * {@link com.adamnickle.deck.CardDrawable} using its current speed
     */
    private Handler mPositionUpdateHandler;
    /**
     * The last time the position of this {@link com.adamnickle.deck.CardDrawable} was updated in
     * {@link com.adamnickle.deck.CardDrawable#mPositionUpdateHandler}
     */
    private long mLastUpdate;
    /**
     * A flag determine whether to let cards move off-screen or not
     */
    private boolean mBounceCard;

    /**
     * Constructs a new {@link com.adamnickle.deck.CardDrawable}
     * @param view The {@link android.view.View} that this {@link com.adamnickle.deck.CardDrawable}
     *             will be drawn to
     * @param resources The {@link android.content.res.Resources} for this application
     * @param card The {@link com.adamnickle.deck.Game.Card} to be drawn
     * @param x The X coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     * @param y The Y coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     * @param reqWidth The minimum required width of the {@link com.adamnickle.deck.CardDrawable}
     * @param reqHeight  The minimum required height of the {@link com.adamnickle.deck.CardDrawable}
     * @param forceSize A flag for if this {@link com.adamnickle.deck.CardDrawable}
     *                  shall be forced to given size
     */
    public CardDrawable( final View view, final Resources resources, final Card card, final int x, final int y, final int reqWidth, final int reqHeight, final boolean forceSize )
    {
        mIsBitmapLoaded = false;
        mCard = card;
        mParent = view;
        mX = x;
        mY = y;
        mPositionUpdateHandler = new Handler();
        mBounceCard = true;

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

    /**
     * Constructs a new {@link com.adamnickle.deck.CardDrawable}
     * Note: The {@link com.adamnickle.deck.CardDrawable} will not be forced to the given size
     * @param view The {@link android.view.View} that this {@link com.adamnickle.deck.CardDrawable}
     *             will be drawn to
     * @param resources The {@link android.content.res.Resources} for this application
     * @param card The {@link com.adamnickle.deck.Game.Card} to be drawn
     * @param x The X coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     * @param y The Y coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     * @param reqWidth The minimum required width of the {@link com.adamnickle.deck.CardDrawable}
     * @param reqHeight  The minimum required height of the {@link com.adamnickle.deck.CardDrawable}
     */
    public CardDrawable( View view, Resources resources, Card card, int x, int y, int reqWidth, int reqHeight )
    {
        this( view, resources, card, x, y, reqWidth, reqHeight, false );
    }

    /**
     * Constructs a new {@link com.adamnickle.deck.CardDrawable}.
     * The reqWidth and reqHeight will be set to
     * {@link com.adamnickle.deck.CardDrawable#DEFAULT_WIDTH} and
     * {@link com.adamnickle.deck.CardDrawable#DEFAULT_WIDTH} respectively.
     * The {@link com.adamnickle.deck.CardDrawable} will not be forced to these sizes
     * @param view The {@link android.view.View} that this {@link com.adamnickle.deck.CardDrawable}
     *             will be drawn to
     * @param resources The {@link android.content.res.Resources} for this application
     * @param card The {@link com.adamnickle.deck.Game.Card} to be drawn
     * @param x The X coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     * @param y The Y coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     */
    public CardDrawable( View view, Resources resources, Card card, int x, int y )
    {
        this( view, resources, card, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT );
    }

    public Card getCard()
    {
        return mCard;
    }

    /**
     * Sets if to prevent cards moving off-screen
     * @param bounceCard The flag determining whether to prevent cards moving off screen or not
     */
    public void setBounceCard( boolean bounceCard )
    {
        mBounceCard = bounceCard;
    }

    /**
     * Gets if cards are being prevented from moving off-screen
     * @return The flag determining if cards are being prevented from moving off-screen
     */
    public boolean getBounceCard()
    {
        return mBounceCard;
    }

    /**
     * Gets whether this {@link com.adamnickle.deck.CardDrawable} is being held or not
     * @return A flag corresponding to if this {@link com.adamnickle.deck.CardDrawable}
     *          is being held or not
     */
    public boolean isHeld()
    {
        return mIsHeld;
    }

    /**
     * Sets whether this card is being held or not
     * @param isHeld The flag stating if this {@link com.adamnickle.deck.CardDrawable}
     *               is being held or not
     */
    public void setIsHeld( boolean isHeld )
    {
        mIsHeld = isHeld;
        if( mIsHeld )
        {
            mVelocityX = 0.0f;
            mVelocityY = 0.0f;
        }
    }

    /**
     * Sets the velocity of the {@link com.adamnickle.deck.CardDrawable}
     * @param velocityX The X component of the velocity
     * @param velocityY The Y component of the velocity
     */
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

    /**
     * Updates the position of the {@link com.adamnickle.deck.CardDrawable} based on its current
     * velocity.
     * Bounces {@link com.adamnickle.deck.CardDrawable} off wall if it has hit one and it set to
     * do so.
     * If it is not set to bounce and the {@link com.adamnickle.deck.CardDrawable} goes off the
     * screen, sets the velocity to 0.
     */
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

        if( getBounceCard() )
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

    /**
     * Returns which side of the screen the {@link com.adamnickle.deck.CardDrawable} has hit or null
     * if it hasn't hit any
     * @return
     */
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

    /**
     * Gets whether the {@link com.adamnickle.deck.CardDrawable} is on the screen or not
     * @return A flag stating if the {@link com.adamnickle.deck.CardDrawable} is on the screen
     */
    private boolean isOnScreen()
    {
        final Rect parentRect = new Rect();
        mParent.getDrawingRect( parentRect );
        return Rect.intersects( parentRect, mDrawRect );
    }

    /**
     * Handles a device orientation change.
     */
    @SuppressWarnings( "SuspiciousNameCombination" )
    public void onOrientationChange()
    {
        final int newTop = mDrawRect.left;
        final int newLeft = mDrawRect.top;
        mDrawRect.set( newLeft, newTop, newLeft + mWidth, newTop + mHeight );
    }

    /**
     * Updates the bounds of the {@link com.adamnickle.deck.CardDrawable} as determine by
     * {@link com.adamnickle.deck.CardDrawable#mDrawRect}
     * using the current position and size
     */
    private void updateBounds()
    {
        if( !mIsBitmapLoaded ) return;

        mDrawRect.offsetTo( (int)( mX - mWidth / 2.0f ), (int)( mY - mHeight / 2.0f ) );
    }

    /**
     * Updates the current position of the {@link com.adamnickle.deck.CardDrawable}
     * @param x The X coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     * @param y The Y coordinate of the initial position of the
     *              {@link com.adamnickle.deck.CardDrawable}
     */
    public void update( int x, int y )
    {
        if( !mIsBitmapLoaded ) return;

        mX = x;
        mY = y;
        updateBounds();
    }

    /**
     * Returns whether the given point is contained within the
     * {@link com.adamnickle.deck.CardDrawable} as being drawn on the screen
     * @param x The X coordinate of the point to check
     * @param y The Y coordinate of the point to check
     * @return
     */
    public boolean contains( int x, int y )
    {
        return mDrawRect.contains( x, y );
    }

    /**
     * Draws the {@link com.adamnickle.deck.CardDrawable} to the given
     * {@link android.graphics.Canvas}
     * @param canvas
     */
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
