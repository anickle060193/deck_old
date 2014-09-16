package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Interfaces.GameUiInterfaceView;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.util.Iterator;
import java.util.LinkedList;

public class GameView extends GameUiInterfaceView
{
    private static final String TAG = "GameView";

    private enum Side { LEFT, TOP, RIGHT, BOTTOM, NONE }

    private static final float MINIMUM_VELOCITY = 400.0f;

    private GestureDetectorCompat mDetector;
    private final LinkedList<CardDrawable> mCardDrawables;
    private SparseArray<CardDrawable> mMovingCardDrawables;

    private final Activity mParentActivity;
    private GameUiListener mListener;
    private final Toast mToast;

    public GameView( Activity activity )
    {
        super( activity );
        Log.d( TAG, "___ CONSTRUCTOR ___" );

        mParentActivity = activity;
        mDetector = new GestureDetectorCompat( activity, mGestureListener );
        mCardDrawables = new LinkedList< CardDrawable >();
        mMovingCardDrawables = new SparseArray< CardDrawable >();

        mToast = Toast.makeText( activity, "", Toast.LENGTH_SHORT );
    }

    @Override
    protected void onAttachedToWindow()
    {
        postDelayed( mUpdateScreen, 10 );
    }

    private Runnable mUpdateScreen = new Runnable()
    {
        @Override
        public void run()
        {
            GameView.this.invalidate();

            postDelayed( this, 10 );
        }
    };

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
            if( mListener != null && cardDrawable != null )
            {
                if( mListener.onAttemptSendCard( cardDrawable.getCard() ) )
                {
                    mCardDrawables.remove( cardDrawable );
                }
            }

            new AlertDialog.Builder( getContext() )
                    .setTitle( "Pick background" )
                    .setItems( R.array.backgrounds, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int index )
                        {
                            final TypedArray resources = getResources().obtainTypedArray( R.array.background_drawables );
                            final int resource = resources.getResourceId( index, -1 );
                            BitmapDrawable background = (BitmapDrawable) getResources().getDrawable( resource );
                            background.setTileModeXY( Shader.TileMode.REPEAT, Shader.TileMode.REPEAT );
                            if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN )
                            {
                                setBackgroundDrawable( background );
                            } else
                            {
                                setBackground( background );
                            }
                        }
                    } )
                    .show();
            return true;
        }
    };

    public void onOrientationChange()
    {
        Log.d( TAG, "__ ORIENTATION CHANGE __" );
        for( CardDrawable cardDrawable : mCardDrawables )
        {
            cardDrawable.onOrientationChange();
        }
    }

    @Override
    public void setGameUiListener( GameUiListener gameUiListener )
    {
        mListener = gameUiListener;
    }

    @Override
    public void addCardDrawable( Card card )
    {
        mCardDrawables.addFirst( new CardDrawable( card, getWidth() / 2, getHeight() / 2 ) );
    }

    @Override
    public boolean removeCardDrawable( Card card )
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
    public void resetCard( Card card )
    {
        for( CardDrawable cardDrawable : mCardDrawables )
        {
            if( cardDrawable.getCard().equals( card ) )
            {
                cardDrawable.resetCardDrawable();
            }
        }
    }

    @Override
    public AlertDialog.Builder createSelectItemDialog( String title, Object[] items, DialogInterface.OnClickListener listener )
    {
        String[] itemNames;
        if( items instanceof String[] )
        {
            itemNames = (String[]) items;
        }
        else
        {
            itemNames = new String[ items.length ];
            for( int i = 0; i < items.length; i++ )
            {
                itemNames[ i ] = items[ i ].toString();
            }
        }
        return new AlertDialog.Builder( mParentActivity )
                .setTitle( title )
                .setItems( itemNames, listener );
    }

    @Override
    public void displayNotification( final String notification )
    {
        mParentActivity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                mToast.setText( notification );
                mToast.show();
            }
        } );
    }

    public class CardDrawable extends Drawable
    {
        private static final String TAG = "CardDrawable";

        private static final float MILLISECONDS_TO_SECONDS = 1.0f / 1000.0f;
        private static final float DECELERATION_RATE = 0.99f;
        private static final float THRESHOLD_VELOCITY = 45.0f;

        private GameUiInterfaceView mGameUiInterfaceView;
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
        private Runnable mPositionUpdateRunnable;
        private long mLastUpdate;
        private boolean mSent;

        public CardDrawable( final Card card, final int x, final int y )
        {
            mIsBitmapLoaded = false;
            mCard = card;
            mGameUiInterfaceView = GameView.this;
            mX = x;
            mY = y;
            mPositionUpdateRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    updateWithVelocity();

                    if( mVelocityX != 0 && mVelocityY != 0 )
                    {
                        postDelayed( this, 10 );
                    }
                }
            };
            mSent = false;

            new Thread()
            {
                @Override
                public void run()
                {
                    final Resources resources = getResources();

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource( resources, card.getResource(), options );
                    mWidth = options.outWidth;
                    mHeight = options.outHeight;

                    options.inJustDecodeBounds = false;
                    mBitmap = BitmapFactory.decodeResource( resources, card.getResource(), options );

                    mDrawRect = new Rect( mX, mY, mX + mWidth, mY + mHeight );
                    mIsBitmapLoaded = true;
                }
            }.start();
        }

        public Card getCard()
        {
            return mCard;
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

        public synchronized void setVelocity( float velocityX, float velocityY )
        {
            mVelocityX = velocityX;
            mVelocityY = velocityY;
            mLastUpdate = System.currentTimeMillis();

            new Thread( mPositionUpdateRunnable ).start();
        }

        public synchronized void resetCardDrawable()
        {
            mX = (int) ( mGameUiInterfaceView.getWidth() / 2.0f );
            mY = (int) ( mGameUiInterfaceView.getHeight() / 2.0f );
            mVelocityX = 0.0f;
            mVelocityY = 0.0f;
        }

        private synchronized void updateWithVelocity()
        {
            final long now = System.currentTimeMillis();
            final long t = now - mLastUpdate;
            final float dx = mVelocityX * ( t * MILLISECONDS_TO_SECONDS );
            final float dy = mVelocityY * ( t * MILLISECONDS_TO_SECONDS );
            mX += (int)dx;
            mY += (int)dy;
            updateBounds();
            mLastUpdate = now;

            if( !mListener.canSendCard() )
            {
                switch( hasHitWall() )
                {
                    case LEFT:
                        final int leftDiff = mGameUiInterfaceView.getLeft() - mDrawRect.left + 1;
                        update( mX + leftDiff, mY );
                        mVelocityX = -mVelocityX;
                        break;
                    case RIGHT:
                        final int rightDiff = mDrawRect.right - mGameUiInterfaceView.getRight() + 1;
                        update( mX - rightDiff, mY );
                        mVelocityX = -mVelocityX;
                        break;
                    case TOP:
                        final int topDiff = mGameUiInterfaceView.getTop() - mDrawRect.top + 1;
                        update( mX, mY + topDiff );
                        mVelocityY = -mVelocityY;
                        break;
                    case BOTTOM:
                        final int bottomDiff = mDrawRect.bottom - mGameUiInterfaceView.getBottom() + 1;
                        update( mX, mY - bottomDiff );
                        mVelocityY = -mVelocityY;
                        break;
                }
            }
            else
            {
                if( !isOnScreen() )
                {
                    if( !mSent && !mListener.onAttemptSendCard( getCard() ) )
                    {
                        resetCardDrawable();
                    }
                    else
                    {
                        mVelocityX = 0.0f;
                        mVelocityY = 0.0f;
                        mSent = true;
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
            } else
            {
                mVelocityX = 0.0f;
                mVelocityY = 0.0f;
            }
        }

        private Side hasHitWall()
        {
            if( mDrawRect.left <= mGameUiInterfaceView.getLeft() )
            {
                return Side.LEFT;
            }
            else if( mDrawRect.top <= mGameUiInterfaceView.getTop() )
            {
                return Side.TOP;
            }
            else if( mDrawRect.right >= mGameUiInterfaceView.getRight() )
            {
                return Side.RIGHT;
            }
            else if( mDrawRect.bottom >= mGameUiInterfaceView.getBottom() )
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
            mGameUiInterfaceView.getDrawingRect( parentRect );
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
            if( !mIsBitmapLoaded ) return;

            mDrawRect.offsetTo( (int)( mX - mWidth / 2.0f ), (int)( mY - mHeight / 2.0f ) );
        }

        public synchronized void update( int x, int y )
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
}
