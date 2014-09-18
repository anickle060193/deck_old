package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Interfaces.GameUiView;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.util.Iterator;
import java.util.LinkedList;

public class GameView extends GameUiView
{
    private static final String TAG = "GameView";

    private static final float MINIMUM_VELOCITY = 400.0f;

    private GestureDetectorCompat mDetector;
    private final LinkedList<CardDrawable> mCardDrawables;
    private SparseArray<CardDrawable> mMovingCardDrawables;

    private final Activity mParentActivity;
    private GameUiListener mListener;
    private final Toast mToast;
    private GameGestureListener mGameGestureListener;

    public GameView( Activity activity )
    {
        super( activity );
        Log.d( TAG, "___ CONSTRUCTOR ___" );

        mParentActivity = activity;
        mDetector = new GestureDetectorCompat( activity, mGestureListener );
        mCardDrawables = new LinkedList< CardDrawable >();
        mMovingCardDrawables = new SparseArray< CardDrawable >();
        mGameGestureListener = mDefaultGameGestureListener;

        mToast = Toast.makeText( activity.getApplicationContext(), "", Toast.LENGTH_SHORT );
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
    public synchronized void onDraw( Canvas canvas )
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

    @Override
    public boolean onTouchEvent( @NonNull MotionEvent event )
    {
        mDetector.onTouchEvent( event );

        final int action = MotionEventCompat.getActionMasked( event );
        switch( action )
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                Log.d( TAG, "--- ACTION_DOWN ---" );

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
                Log.d( TAG, "--- ACTION UP ---" );

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
            Log.d( TAG, "+++ ON DOWN +++" );

            return true;
        }

        @Override
        public boolean onFling( MotionEvent event1, MotionEvent event2, float velocityX, float velocityY )
        {
            Log.d( TAG, "+++ ON FLING +++" );

            final float velocity = (float)Math.sqrt( velocityX * velocityX + velocityY * velocityY );
            if( velocity > MINIMUM_VELOCITY )
            {
                final int pointerIndex = MotionEventCompat.getActionIndex( event2 );
                final int pointerId = MotionEventCompat.getPointerId( event2, pointerIndex );
                final CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                if( cardDrawable != null )
                {
                    cardDrawable.setVelocity( velocityX, velocityY );

                    if( mGameGestureListener != null )
                    {
                        mGameGestureListener.onCardFling( event1, event2, cardDrawable );
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY )
        {
            Log.d( TAG, "+++ ON SCROLL +++" );
            for( int i = 0; i < MotionEventCompat.getPointerCount( e2 ); i++ )
            {
                final int pointerId = MotionEventCompat.getPointerId( e2, i );
                final float x = MotionEventCompat.getX( e2, i );
                final float y = MotionEventCompat.getY( e2, i );

                CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                if( cardDrawable != null )
                {
                    cardDrawable.update( (int) x, (int) y );

                    if( mGameGestureListener != null )
                    {
                        mGameGestureListener.onCardMove( e1, e2, cardDrawable );
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed( MotionEvent event )
        {
            Log.d( TAG, "+++ ON SINGLE TAP CONFIRMED +++" );

            final int x = (int) event.getX();
            final int y = (int) event.getY();

            for( CardDrawable cardDrawable : mCardDrawables )
            {
                if( cardDrawable != null && cardDrawable.contains( x, y ) )
                {
                    if( mGameGestureListener != null )
                    {
                        mGameGestureListener.onCardSingleTap( event, cardDrawable );
                    }
                    break;
                }
            }

            return true;
        }

        @Override
        public boolean onDoubleTap( MotionEvent event )
        {
            Log.d( TAG, "+++ ON DOUBLE TAP +++" );

            final int x = (int) event.getX();
            final int y = (int) event.getY();

            boolean foundCard = false;
            for( CardDrawable cardDrawable : mCardDrawables )
            {
                if( cardDrawable != null && cardDrawable.contains( x, y ) )
                {
                    cardDrawable.flipFaceUp();

                    if( mGameGestureListener != null )
                    {
                        foundCard = true;
                        mGameGestureListener.onCardDoubleTap( event, cardDrawable );
                    }
                    break;
                }
            }

            if( !foundCard )
            {
                if( mGameGestureListener != null )
                {
                    mGameGestureListener.onGameDoubleTap( event );
                }
            }

            return true;
        }
    };

    private GameGestureListener mDefaultGameGestureListener = new GameGestureListener()
    {
        @Override
        public void onCardSingleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            cardDrawable.flipFaceUp();
        }

        @Override
        public void onGameDoubleTap( MotionEvent event )
        {
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
    public void setGameGestureListener( GameGestureListener gameGestureListener )
    {
        mGameGestureListener = gameGestureListener;
    }

    @Override
    public void addCardDrawable( Card card )
    {
        mCardDrawables.addFirst( new CardDrawable( this, mListener, card ) );
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
    public synchronized void removeAllCardDrawables()
    {
        mCardDrawables.clear();
    }

    @Override
    public void resetCard( Card card )
    {
        for( CardDrawable cardDrawable : mCardDrawables )
        {
            if( cardDrawable.getCard().equals( card ) )
            {
                cardDrawable.resetCardDrawable();
                break;
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
    public void showPopup( String title, String message )
    {
        new AlertDialog.Builder( mParentActivity )
                .setTitle( title )
                .setMessage( message )
                .setPositiveButton( "OK", null )
                .show();
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
}
