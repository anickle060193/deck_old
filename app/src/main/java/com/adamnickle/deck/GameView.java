package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.DeckSettings;
import com.adamnickle.deck.Interfaces.CardHolderListener;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.adamnickle.deck.Interfaces.GameUiView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class GameView extends GameUiView
{
    private static final float MINIMUM_VELOCITY = 400.0f;

    private GestureDetector mDetector;
    protected final LinkedList< CardDrawable > mCardDrawables;
    protected HashMap< Integer, CardDrawable > mMovingCardDrawables;
    protected HashMap< String, ArrayList< CardDrawable > > mCardDrawablesByOwners;

    protected final Activity mParentActivity;
    protected GameUiListener mListener;
    protected final Toast mToast;
    protected GameGestureListener mGameGestureListener;

    public GameView( Activity activity )
    {
        super( activity );

        mParentActivity = activity;
        mDetector = new GestureDetector( activity, mGestureListener );
        mCardDrawables = new LinkedList< CardDrawable >();
        mMovingCardDrawables = new HashMap< Integer, CardDrawable >();
        mCardDrawablesByOwners = new HashMap< String, ArrayList< CardDrawable > >();
        mGameGestureListener = mDefaultGameGestureListener;

        mToast = Toast.makeText( activity.getApplicationContext(), "", Toast.LENGTH_SHORT );

        final String background = PreferenceManager.getDefaultSharedPreferences( getContext().getApplicationContext() ).getString( DeckSettings.BACKGROUND, "White" );
        final String[] backgrounds = getResources().getStringArray( R.array.backgrounds );
        setGameBackground( Arrays.asList( backgrounds ).indexOf( background ) );
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                GameView.this.invalidate();
                postDelayed( this, 10 );
            }
        }, 10 );
    }

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
    public synchronized boolean onTouchEvent( MotionEvent event )
    {
        mDetector.onTouchEvent( event );

        final int action = event.getActionMasked();
        switch( action )
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {

                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId( pointerIndex );
                final float x = event.getX( pointerIndex );
                final float y = event.getY( pointerIndex );

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
                for( CardDrawable cardDrawable : mMovingCardDrawables.values() )
                {
                    cardDrawable.setIsHeld( false );
                }
                mMovingCardDrawables.clear();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId( pointerIndex );
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
            final float velocity = (float) Math.sqrt( velocityX * velocityX + velocityY * velocityY );
            if( velocity > MINIMUM_VELOCITY )
            {
                final int pointerIndex = event2.getActionIndex();
                final int pointerId = event2.getPointerId( pointerIndex );
                final CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                if( cardDrawable != null )
                {
                    if( mGameGestureListener != null )
                    {
                        if( !mGameGestureListener.onCardFling( event1, event2, cardDrawable, velocityX, velocityY ) )
                        {
                            return mDefaultGameGestureListener.onCardFling( event1, event2, cardDrawable, velocityX, velocityY );
                        }
                        else
                        {
                            return true;
                        }
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY )
        {
            for( int i = 0; i < e2.getPointerCount(); i++ )
            {
                final int pointerId = e2.getPointerId( i );
                final float x = e2.getX( i );
                final float y = e2.getY( i );

                CardDrawable cardDrawable = mMovingCardDrawables.get( pointerId );
                if( cardDrawable != null )
                {
                    if( mGameGestureListener != null )
                    {
                        if( !mGameGestureListener.onCardMove( e1, e2, cardDrawable, x, y ) )
                        {
                            return mDefaultGameGestureListener.onCardMove( e1, e2, cardDrawable, x, y );
                        }
                        else
                        {
                            return true;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed( MotionEvent event )
        {
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            for( CardDrawable cardDrawable : mCardDrawables )
            {
                if( cardDrawable != null && cardDrawable.contains( x, y ) )
                {
                    if( mGameGestureListener != null )
                    {
                        if( !mGameGestureListener.onCardSingleTap( event, cardDrawable ) )
                        {
                            return mDefaultGameGestureListener.onCardSingleTap( event, cardDrawable );
                        }
                        else
                        {
                            return true;
                        }
                    }
                    break;
                }
            }

            return true;
        }

        @Override
        public boolean onDoubleTap( MotionEvent event )
        {
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            for( CardDrawable cardDrawable : mCardDrawables )
            {
                if( cardDrawable != null && cardDrawable.contains( x, y ) )
                {
                    cardDrawable.flipFaceUp();

                    if( mGameGestureListener != null )
                    {
                        if( !mGameGestureListener.onCardDoubleTap( event, cardDrawable ))
                        {
                            return mDefaultGameGestureListener.onCardDoubleTap( event, cardDrawable );
                        }
                        else
                        {
                            return true;
                        }
                    }
                    break;
                }
            }

            if( mGameGestureListener != null )
            {
                if( !mGameGestureListener.onGameDoubleTap( event ) )
                {
                    return mDefaultGameGestureListener.onGameDoubleTap( event );
                }
                else
                {
                    return true;
                }
            }

            return false;
        }
    };

    private GameGestureListener mDefaultGameGestureListener = new GameGestureListener()
    {
        @Override
        public boolean onCardMove( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float x, float y )
        {
            cardDrawable.update( (int) x, (int) y );
            return true;
        }

        @Override
        public boolean onCardFling( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float velocityX, float velocityY )
        {
            cardDrawable.setVelocity( velocityX, velocityY );
            return true;
        }

        @Override
        public boolean onCardSingleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            cardDrawable.flipFaceUp();
            return true;
        }

        @Override
        public boolean onGameDoubleTap( MotionEvent event )
        {
            new AlertDialog.Builder( getContext() )
                    .setTitle( "Pick background" )
                    .setItems( R.array.backgrounds, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int index )
                        {
                            final String[] backgrounds = getResources().getStringArray( R.array.backgrounds );
                            final String background = backgrounds[ index ];
                            PreferenceManager
                                    .getDefaultSharedPreferences( getContext().getApplicationContext() )
                                    .edit()
                                    .putString( DeckSettings.BACKGROUND, background )
                                    .commit();
                            setGameBackground( index );
                        }
                    } )
                    .show();
            return true;
        }
    };

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

    protected final CardHolderListener mDefaultCardHolderListener = new CardHolderListener()
    {
        @Override
        public void onCardRemoved( String playerID, Card card )
        {
            ArrayList<CardDrawable> cardDrawables = mCardDrawablesByOwners.get( playerID );
            if( cardDrawables != null )
            {
                CardDrawable removeCardDrawable = null;
                for( CardDrawable cardDrawable : cardDrawables )
                {
                    if( cardDrawable.getCard().equals( card ) )
                    {
                        removeCardDrawable = cardDrawable;
                        break;
                    }
                }
                if( removeCardDrawable != null )
                {
                    mCardDrawables.remove( removeCardDrawable );
                }
            }
        }

        @Override
        public void onCardsRemoved( String playerID, Card[] cards )
        {
            Arrays.sort( cards, new Card.CardComparator( CardCollection.SortingType.SORT_BY_CARD_NUMBER ) );

            final ArrayList<CardDrawable> cardDrawables = mCardDrawablesByOwners.get( playerID );
            final Iterator<CardDrawable> cardDrawableIterator = cardDrawables.iterator();
            final ArrayList< CardDrawable > removedCards = new ArrayList< CardDrawable >();

            while( cardDrawableIterator.hasNext() && ( removedCards.size() < cards.length ) )
            {
                final CardDrawable cardDrawable = cardDrawableIterator.next();
                if( Arrays.binarySearch( cards, cardDrawable.getCard() ) >= 0 )
                {
                    removedCards.add( cardDrawable );
                    cardDrawableIterator.remove();
                }
            }
            mCardDrawables.removeAll( removedCards );
        }

        @Override
        public void onCardAdded( String playerID, Card card )
        {
            final CardDrawable cardDrawable = new CardDrawable( GameView.this, mListener, playerID, card );
            mCardDrawables.addFirst( cardDrawable );

            ArrayList<CardDrawable> cardDrawables;
            if( !mCardDrawablesByOwners.containsKey( playerID ) )
            {
                cardDrawables = new ArrayList< CardDrawable >();
                mCardDrawablesByOwners.put( playerID, cardDrawables );
            }
            else
            {
                cardDrawables = mCardDrawablesByOwners.get( playerID );
            }
            cardDrawables.add( cardDrawable );
        }

        @Override
        public void onCardsAdded( String playerID, Card[] cards )
        {
            ArrayList<CardDrawable> cardDrawables;
            if( !mCardDrawablesByOwners.containsKey( playerID ) )
            {
                cardDrawables = new ArrayList< CardDrawable >();
                mCardDrawablesByOwners.put( playerID, cardDrawables );
            }
            else
            {
                cardDrawables = mCardDrawablesByOwners.get( playerID );
            }


            for( Card card : cards )
            {
                final CardDrawable cardDrawable = new CardDrawable( GameView.this, mListener, playerID, card );
                mCardDrawables.add( cardDrawable );
                cardDrawables.add( cardDrawable );
            }
        }

        @Override
        public void onCardsCleared( String cardHolderID )
        {
            final ArrayList< CardDrawable > cardDrawables = mCardDrawablesByOwners.remove( cardHolderID );
            if( cardDrawables != null )
            {
                mCardDrawables.removeAll( cardDrawables );
            }
        }
    };

    @Override
    public CardHolderListener getCardHolderListener()
    {
        return mDefaultCardHolderListener;
    }

    @Override
    public synchronized void resetCard( String cardHolderID, Card card )
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
    public synchronized void sortCards( String cardHolderID, CardCollection.SortingType sortingType )
    {
        Collections.sort( mCardDrawables, new CardDrawable.CardDrawableComparator( sortingType ) );
    }

    @Override
    public synchronized void layoutCards( String cardHolderID )
    {
        if( mCardDrawables.size() == 0 )
        {
            return;
        }

        final int cardWidth = mCardDrawables.get( 0 ).getWidth();
        final int cardHeight = mCardDrawables.get( 0 ).getHeight();
        final int cardHeaderHeight = (int) ( cardHeight * CardDrawable.CARD_HEADER_PERCENTAGE );

        final int OFFSET = 30;

        final int cardsPerColumn = (int) ( (float) ( this.getHeight() - OFFSET - ( cardHeight - cardHeaderHeight) ) / cardHeaderHeight );
        final int cardsPerRow = this.getWidth() / ( OFFSET + cardWidth );

        if( cardsPerColumn * cardsPerRow < mCardDrawables.size() )
        {
            this.showPopup( "Cannot layout cards", "There is not enough room to layout cards...sorry." );
            return;
        }

        int i = 0;
        Iterator <CardDrawable> cardDrawableIterator = mCardDrawables.descendingIterator();
        while( cardDrawableIterator.hasNext() )
        {
            final CardDrawable cardDrawable = cardDrawableIterator.next();
            final int xDisplacement = i / cardsPerColumn;
            final int x = OFFSET + cardWidth / 2 + ( xDisplacement ) * ( OFFSET + cardWidth );
            final int yDisplacement = i % cardsPerColumn;
            final int y = OFFSET + cardHeight / 2 + ( yDisplacement ) * ( cardHeaderHeight );
            cardDrawable.setVelocity( 0, 0 );
            cardDrawable.update( x, y );
            i++;
        }
    }

    @Override
    public AlertDialog.Builder createEditTextDialog( String title, String preSetText, String positiveButtonText, String negativeButtonText, final OnEditTextDialogClickListener onClickListener )
    {
        final EditText editText = new EditText( mParentActivity );

        AlertDialog.Builder builder = new AlertDialog.Builder( mParentActivity )
                .setTitle( title )
                .setView( editText );

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialogInterface, int i )
            {
                switch( i )
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        onClickListener.onPositiveButtonClick( dialogInterface, editText.getText().toString() );
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        onClickListener.onNegativeButtonClick( dialogInterface );
                        break;
                }
            }
        };

        if( positiveButtonText != null && !positiveButtonText.isEmpty())
        {
            builder.setPositiveButton( positiveButtonText, clickListener);
        }

        if( negativeButtonText != null && !negativeButtonText.isEmpty() )
        {
            builder.setNegativeButton( negativeButtonText, clickListener);
        }

        return builder;
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
