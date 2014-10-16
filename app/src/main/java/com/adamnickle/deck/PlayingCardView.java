package com.adamnickle.deck;


import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.squareup.picasso.Picasso;

public class PlayingCardView extends ImageView
{
    private final GestureDetector mGestureDetector;
    private final GameUiListener mListener;
    private final Card mCard;
    private String mOwnerID;

    public PlayingCardView( Context context, GameUiListener gameUiListener, String ownerID, Card card )
    {
        super( context );

        mGestureDetector = new GestureDetector( getContext(), mGestureListener);
        mListener = gameUiListener;
        mCard = card;
        mOwnerID = ownerID;

        this.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );

        Picasso.with( getContext() ).load( mCard.getResource() ).into( this );
    }

    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener()
    {
        @Override
        public boolean onDown( MotionEvent event )
        {
            return true;
        }

        @Override
        public void onShowPress( MotionEvent event )
        {

        }

        @Override
        public boolean onSingleTapUp( MotionEvent event )
        {
            return false;
        }

        @Override
        public boolean onScroll( MotionEvent event, MotionEvent event2, float dX, float dY )
        {
            PlayingCardView.this.setX( PlayingCardView.this.getX() - dX );
            PlayingCardView.this.setY( PlayingCardView.this.getY() - dY );
            return true;
        }

        @Override
        public void onLongPress( MotionEvent event )
        {

        }

        @Override
        public boolean onFling( MotionEvent event, MotionEvent event2, float xVelocity, float yVelocity )
        {
            return false;
        }
    };

    @Override
    public synchronized boolean onTouchEvent( MotionEvent event )
    {
        return mGestureDetector.onTouchEvent( event );
    }
}
