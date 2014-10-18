package com.adamnickle.deck;


import android.content.Context;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Comparator;

public class PlayingCardView extends ImageView
{
    private static final float MINIMUM_VELOCITY = 50.0f;

    private final Card mCard;
    private String mOwnerID;
    private boolean mFaceUp;
    private boolean mFirstLayout;
    private boolean mPreventLayout;
    private float mVelocityX;
    private float mVelocityY;
    private long mLastUpdate;
    private Runnable mFlingUpdater;

    public PlayingCardView( Context context, String ownerID, Card card )
    {
        super( context );

        mCard = card;
        mOwnerID = ownerID;
        mFaceUp = false;
        mFirstLayout = true;
        mPreventLayout = false;
        mVelocityX = 0.0f;
        mVelocityY = 0.0f;

        Picasso.with( getContext() ).load( CardResources.BLUE_CARD_BACK ).into( this );

        this.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );

        mFlingUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized( PlayingCardView.this )
                {
                    final long now = System.currentTimeMillis();
                    final float elapsedTime = now - mLastUpdate;
                    final int dx = (int) ( ( mVelocityX * elapsedTime ) / 1000.0f );
                    final int dy = (int) ( ( mVelocityY * elapsedTime ) / 1000.0f );
                    PlayingCardView.this.offsetLeftAndRight( dx );
                    PlayingCardView.this.offsetTopAndBottom( dy );
                    mVelocityX *= 0.99f;
                    mVelocityY *= 0.99f;

                    final ViewGroup parent = (ViewGroup) PlayingCardView.this.getParent();
                    if( !PlayingCardView.this.isOnScreen() )
                    {
                        mVelocityX = 0.0f;
                        mVelocityY = 0.0f;
                        if( parent instanceof CardDisplayLayout )
                        {
                            ( (CardDisplayLayout) parent ).childViewOffScreen( PlayingCardView.this );
                        }
                    }

                    switch( hasHitWall() )
                    {
                        case LEFT:
                            mVelocityX = -mVelocityX;
                            setX( 0 );
                            break;

                        case RIGHT:
                            mVelocityX = -mVelocityX;
                            offsetLeftAndRight( parent.getRight() - getRight() );
                            break;

                        case TOP:
                            mVelocityY = -mVelocityY;
                            setY( 0 );
                            break;

                        case BOTTOM:
                            mVelocityY = -mVelocityY;
                            offsetTopAndBottom( parent.getBottom() - getBottom() );
                            break;
                    }


                    if( Math.abs( mVelocityX ) < MINIMUM_VELOCITY )
                    {
                        mVelocityX = 0.0f;
                    }
                    if( Math.abs( mVelocityY ) < MINIMUM_VELOCITY )
                    {
                        mVelocityY = 0.0f;
                    }
                    if( mVelocityX != 0.0f && mVelocityY != 0.0f )
                    {
                        mLastUpdate = now;
                        PlayingCardView.this.postDelayed( this, 10 );
                    }
                }
            }
        };
    }

    public synchronized void onTouched()
    {
        mVelocityX = 0.0f;
        mVelocityY = 0.0f;
    }

    public synchronized void fling( float xVelocity, float yVelocity )
    {
        mVelocityX = Math.min( xVelocity / 2.0f, ViewConfiguration.get( getContext() ).getScaledMaximumFlingVelocity() );
        mVelocityY = Math.min( yVelocity / 2.0f, ViewConfiguration.get( getContext() ).getScaledMaximumFlingVelocity() );

        mLastUpdate = System.currentTimeMillis();
        this.post( mFlingUpdater );
    }

    @Override
    public void setX( float x )
    {
        this.offsetLeftAndRight( (int) ( x - this.getLeft() ) );
    }

    @Override
    public void setY( float y )
    {
        this.offsetTopAndBottom( (int) ( y - this.getTop() ) );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );

        if( mFirstLayout )
        {
            mFirstLayout = false;
            reset();
        }
    }

    private synchronized boolean isOnScreen()
    {
        final ViewGroup parent = (ViewGroup) this.getParent();
        if( parent == null )
        {
            return true;
        }
        else if( this.getRight() <= 0 )
        {
            return false;
        }
        else if( this.getLeft() >= parent.getWidth() )
        {
            return false;
        }
        else if( this.getBottom() <= 0 )
        {
            return false;
        }
        else if( this.getTop() >= parent.getHeight() )
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private CardDisplayLayout.Side hasHitWall()
    {
        final ViewGroup parent = (ViewGroup) this.getParent();
        if( this.getLeft() <= parent.getLeft() )
        {
            return CardDisplayLayout.Side.LEFT;
        }
        else if( this.getTop() <= parent.getTop() )
        {
            return CardDisplayLayout.Side.TOP;
        }
        else if( this.getRight() >= parent.getRight() )
        {
            return CardDisplayLayout.Side.RIGHT;
        }
        else if( this.getBottom() >= parent.getBottom() )
        {
            return CardDisplayLayout.Side.BOTTOM;
        }
        else
        {
            return CardDisplayLayout.Side.NONE;
        }
    }

    @Override
    public void requestLayout()
    {
        if( !mPreventLayout)
        {
            super.requestLayout();
        }
        else
        {
            Log.e( "PlayingCardView", "--- BLOCKED FROM REQUEST_LAYOUT ---" );
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

    public void flipFaceUp()
    {
        if( !mFaceUp )
        {
            mFaceUp = true;
            mPreventLayout = true;
            Picasso.with( getContext() ).load( mCard.getResource() ).noFade().placeholder( this.getDrawable() ).into( this, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    mPreventLayout = false;
                }

                @Override
                public void onError()
                {
                    mPreventLayout = false;
                }
            } );
        }
    }

    public synchronized void reset()
    {
        mVelocityX = 0.0f;
        mVelocityX = 0.0f;

        final ViewGroup parent = (ViewGroup) this.getParent();
        final int width = parent.getWidth();
        final float widthHalf = width * 0.5f;
        final int height = parent.getHeight();
        final float heightHalf = height * 0.5f;
        final int randomXOffset = (int) ( Math.random() * widthHalf - 0.5f * widthHalf );
        final int randomYOffset = (int) ( Math.random() * heightHalf - 0.5f * heightHalf );
        final int newX = (int) ( ( width - this.getWidth() ) / 2.0f + randomXOffset );
        final int newY = (int) ( ( height - this.getHeight() ) / 2.0f + randomYOffset );

        this.setX( newX );
        this.setY( newY );
    }

    public static class PlayingCardViewComparator implements Comparator< PlayingCardView >
    {
        private final Card.CardComparator mCardComparator;

        public PlayingCardViewComparator( CardCollection.SortingType sortType )
        {
            mCardComparator = new Card.CardComparator( sortType );
        }

        @Override
        public int compare( PlayingCardView cardView, PlayingCardView cardView2 )
        {
            return mCardComparator.compare( cardView.getCard(), cardView2.getCard() );
        }
    }
}
