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

public class CardDrawable extends Drawable
{
    private static final String TAG = "CardDrawable";

    public static final int DEFAULT_WIDTH = 598;
    public static final int DEFAULT_HEIGHT = 834;

    private static final float TIME_CONVERT = 1000.0f;

    private View mParent;
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

    public CardDrawable( final View view, final Resources resources, final int resource, final int x, final int y, final int reqWidth, final int reqHeight, final boolean forceSize )
    {
        mIsBitmapLoaded = false;
        mParent = view;
        mX = x;
        mY = y;
        mPositionUpdateHandler = new Handler();

        new Thread()
        {
            @Override
            public void run()
            {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource( resources, resource, options );

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
                BitmapFactory.decodeResource( resources, resource, options );
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
                mBitmap = BitmapFactory.decodeResource( resources, resource, options );

                mDrawRect = new Rect( mX, mY, mX + mWidth, mY + mHeight );
                mIsBitmapLoaded = true;
            }
        }.start();
    }

    public CardDrawable( View view, Resources resources, int resource, int x, int y, int reqWidth, int reqHeight )
    {
        this( view, resources, resource, x, y, reqWidth, reqHeight, false );
    }

    public CardDrawable( View view, Resources resources, int resource, int x, int y )
    {
        this( view, resources, resource, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT );
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

        if( !isOnScreen() )
        {
            mVelocityX = 0.0f;
            mVelocityY = 0.0f;
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
