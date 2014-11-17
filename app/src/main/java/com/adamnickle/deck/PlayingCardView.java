package com.adamnickle.deck;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;
import com.adamnickle.deck.Game.DeckSettings;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

import ru.noties.debug.Debug;

public class PlayingCardView extends ImageView
{
    private static final float MINIMUM_VELOCITY = 50.0f;

    private static final HashMap< Float, Bitmap > sScaledBackBitmaps = new HashMap< Float, Bitmap >();
    private static int sCardBackResource = 0;

    private Bitmap mCardBitmap;
    private final Card mCard;
    private String mOwnerID;
    private boolean mFaceUp;
    private boolean mResetCard;
    private float mVelocityX;
    private float mVelocityY;
    private long mLastUpdate;
    private float mScale;
    private boolean mAttachedToWindow;
    private boolean mBitmapLoaded;

    public PlayingCardView( final Context context, String ownerID, Card card, float scale )
    {
        super( context );

        mCard = card;
        mOwnerID = ownerID;
        mFaceUp = false;
        mResetCard = true;
        mVelocityX = 0.0f;
        mVelocityY = 0.0f;
        mScale = Math.round( scale * 100.0f ) / 100.0f;
        mAttachedToWindow = false;
        mBitmapLoaded = false;

        this.setLayoutParams( new CardDisplayLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
        this.setScaleType( ScaleType.CENTER_CROP );

        boolean hasBackground = false;
        if( mOwnerID.startsWith( TableFragment.DISCARD_PILE_ID_PREFIX ) )
        {
            this.setBackgroundResource( R.drawable.discard_pile_outline );
            hasBackground = true;
        }
        else if( mOwnerID.startsWith( TableFragment.DRAW_PILE_ID_PREFIX ) )
        {
            this.setBackgroundResource( R.drawable.draw_pile_outline );
            hasBackground = true;
        }

        if( hasBackground )
        {
            final int cardOutlineWidth = getResources().getDimensionPixelSize( R.dimen.card_outline_width );
            final int cardOutlineSpace = getResources().getDimensionPixelSize( R.dimen.card_outline_space );
            final int padding = cardOutlineWidth + cardOutlineSpace;
            this.setPadding( padding, padding, padding, padding );
        }

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    final int cardWidth = (int) ( getResources().getDimensionPixelSize( R.dimen.card_width ) * mScale );
                    final int cardHeight = (int) ( getResources().getDimensionPixelSize( R.dimen.card_height ) * mScale );

                    synchronized( PlayingCardView.class )
                    {
                        final int cardBackResource = DeckSettings.getCardBackResource( getContext() );
                        if( cardBackResource != sCardBackResource )
                        {
                            sCardBackResource = cardBackResource;
                            for( Bitmap bitmap : sScaledBackBitmaps.values() )
                            {
                                bitmap.recycle();
                            }
                            sScaledBackBitmaps.clear();
                        }
                        if( !sScaledBackBitmaps.containsKey( mScale ) )
                        {
                            final Bitmap bitmap = Picasso
                                    .with( getContext() )
                                    .load( sCardBackResource )
                                    .resize( cardWidth, cardHeight )
                                    .get();
                            sScaledBackBitmaps.put( mScale, bitmap );
                        }
                    }
                    mCardBitmap = Picasso
                            .with( getContext() )
                            .load( mCard.getResource() )
                            .resize( cardWidth, cardHeight )
                            .get();
                    mBitmapLoaded = true;

                    new Handler( Looper.getMainLooper() ).post( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setCardBitmap();
                        }
                    } );
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public PlayingCardView( Context context, String ownerID, Card card, float x, float y )
    {
        this( context, ownerID, card );
        this.setX( x );
        this.setY( y );
    }

    public PlayingCardView( Context context, String ownerID, Card card )
    {
        this( context, ownerID, card, 1.0f );
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

    private Runnable mFlingUpdater = new Runnable()
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
                CardDisplayLayout.LayoutParams lp = (CardDisplayLayout.LayoutParams) PlayingCardView.this.getLayoutParams();
                lp.Left += dx;
                lp.Top += dy;
                mVelocityX *= 0.99f;
                mVelocityY *= 0.99f;

                final CardDisplayLayout parent = (CardDisplayLayout) PlayingCardView.this.getParent();

                if( parent == null )
                {
                    PlayingCardView.this.stop();
                }

                final CardDisplayLayout.Side side = PlayingCardView.this.hasHitWall();

                if( side != CardDisplayLayout.Side.NONE && parent.childShouldBounce( PlayingCardView.this, side ) )
                {
                    switch( side )
                    {
                        case LEFT:
                            mVelocityX = -mVelocityX;
                            PlayingCardView.this.setX( 0 );
                            break;

                        case RIGHT:
                            mVelocityX = -mVelocityX;
                            PlayingCardView.this.offsetLeftAndRight( parent.getRight() - getRight() );
                            break;

                        case TOP:
                            mVelocityY = -mVelocityY;
                            PlayingCardView.this.setY( 0 );
                            break;

                        case BOTTOM:
                            mVelocityY = -mVelocityY;
                            PlayingCardView.this.offsetTopAndBottom( parent.getBottom() - getBottom() );
                            break;
                    }
                }

                final CardDisplayLayout.Side offSide = PlayingCardView.this.getWallSlidPast();
                if( offSide != CardDisplayLayout.Side.NONE )
                {
                    Debug.d( offSide.toString() );
                    PlayingCardView.this.stop();
                    parent.childViewOffScreen( PlayingCardView.this, offSide );
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

    public synchronized void onTouched()
    {
        this.stop();
    }

    public synchronized void fling( float xVelocity, float yVelocity )
    {
        mVelocityX = Math.min( xVelocity / 2.0f, ViewConfiguration.get( getContext() ).getScaledMaximumFlingVelocity() );
        mVelocityY = Math.min( yVelocity / 2.0f, ViewConfiguration.get( getContext() ).getScaledMaximumFlingVelocity() );

        mLastUpdate = System.currentTimeMillis();
        this.post( mFlingUpdater );
    }

    public synchronized void stop()
    {
        mVelocityX = 0.0f;
        mVelocityY = 0.0f;
    }

    @Override
    public void setX( float x )
    {
        mResetCard = false;
        this.offsetLeftAndRight( (int) ( x - this.getLeft() ) );
        CardDisplayLayout.LayoutParams lp = (CardDisplayLayout.LayoutParams) this.getLayoutParams();
        lp.Left = (int) x;
    }

    @Override
    public void setY( float y )
    {
        mResetCard = false;
        this.offsetTopAndBottom( (int) ( y - this.getTop() ) );
        CardDisplayLayout.LayoutParams lp = (CardDisplayLayout.LayoutParams) this.getLayoutParams();
        lp.Top = (int) y;
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );

        if( mResetCard && mBitmapLoaded )
        {
            mResetCard = false;
            reset();
        }
    }

    private CardDisplayLayout.Side getWallSlidPast()
    {
        final ViewGroup parent = (ViewGroup) this.getParent();
        if( parent == null )
        {
            return CardDisplayLayout.Side.NONE;
        }
        else if( this.getRight() < 0 )
        {
            return CardDisplayLayout.Side.LEFT;
        }
        else if( this.getBottom() < 0 )
        {
            return CardDisplayLayout.Side.TOP;
        }
        else if( this.getLeft() > parent.getWidth() )
        {
            return CardDisplayLayout.Side.RIGHT;
        }
        else if( this.getTop() > parent.getHeight() )
        {
            return CardDisplayLayout.Side.BOTTOM;
        }
        else
        {
            return CardDisplayLayout.Side.NONE;
        }
    }

    private CardDisplayLayout.Side hasHitWall()
    {
        final ViewGroup parent = (ViewGroup) this.getParent();
        if( parent == null )
        {
            return CardDisplayLayout.Side.NONE;
        }
        else if( this.getLeft() <= 0 )
        {
            return CardDisplayLayout.Side.LEFT;
        }
        else if( this.getTop() <= 0 )
        {
            return CardDisplayLayout.Side.TOP;
        }
        else if( this.getRight() >= parent.getWidth() )
        {
            return CardDisplayLayout.Side.RIGHT;
        }
        else if( this.getBottom() >= parent.getHeight() )
        {
            return CardDisplayLayout.Side.BOTTOM;
        }
        else
        {
            return CardDisplayLayout.Side.NONE;
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

    public void flip()
    {
        this.flip( !mFaceUp );
    }

    public void flip( boolean faceUp )
    {
        this.flip( faceUp, true );
    }

    public void flip( boolean faceUp, boolean animate )
    {
        if( faceUp != mFaceUp )
        {
            if( mAttachedToWindow && animate )
            {
                final AnimationSet toMiddle = (AnimationSet) AnimationUtils.loadAnimation( getContext(), R.anim.flip_first_half );
                final AnimationSet fromMiddle = (AnimationSet) AnimationUtils.loadAnimation( getContext(), R.anim.flip_last_half );

                toMiddle.getAnimations().get( 0 ).setAnimationListener( new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationEnd( Animation animation )
                    {
                        mFaceUp = !mFaceUp;
                        PlayingCardView.this.setCardBitmap();
                        PlayingCardView.this.clearAnimation();
                        PlayingCardView.this.startAnimation( fromMiddle );
                    }

                    @Override
                    public void onAnimationStart( Animation animation )
                    {
                    }

                    @Override
                    public void onAnimationRepeat( Animation animation )
                    {
                    }
                } );

                this.startAnimation( toMiddle );
            }
            else
            {
                mFaceUp = !mFaceUp;
                setCardBitmap();
            }
        }
    }

    private void setCardBitmap()
    {
        if( mFaceUp )
        {
            if( mCardBitmap != null )
            {
                this.setImageBitmap( mCardBitmap );
            }
        }
        else
        {
            if( sScaledBackBitmaps.containsKey( mScale ) )
            {
                this.setImageBitmap( sScaledBackBitmaps.get( mScale ) );
            }
        }
    }

    public synchronized void reset()
    {
        this.stop();

        final ViewGroup parent = (ViewGroup) this.getParent();
        final int parentWidth = parent.getWidth();
        final int parentHeight = parent.getHeight();
        final int width = this.getWidth();
        final int height = this.getHeight();
        final float horizontalOffsetRange = parentWidth * 0.2f;
        final float verticalOffsetRange = parentHeight * 0.2f;
        final float randomXOffset = (float) ( horizontalOffsetRange * Math.random() - horizontalOffsetRange / 2.0f );
        final float randomYOffset = (float) ( verticalOffsetRange * Math.random() - verticalOffsetRange / 2.0f );
        final float newX = ( parentWidth - width ) / 2.0f + randomXOffset;
        final float newY = ( parentHeight - height ) / 2.0f + randomYOffset;

        this.setX( newX );
        this.setY( newY );
    }

    public boolean contains( float x, float y )
    {
        return this.getLeft() <= x && x <= this.getRight() && this.getTop() <= y && y <= this.getBottom();
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
