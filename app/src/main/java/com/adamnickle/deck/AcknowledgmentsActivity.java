package com.adamnickle.deck;

import android.content.Intent;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adamnickle.deck.Game.Card;

import java.util.ArrayList;


public class AcknowledgmentsActivity extends ActionBarActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        final RecyclerView recyclerView = new RecyclerView( this );
        recyclerView.setAdapter( new AcknowledgmentsAdapter() );
        recyclerView.setLayoutManager( new LinearLayoutManager( this ) );

        setContentView( recyclerView );

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    public void browseTo( int linkStringResource )
    {
        Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( getResources().getString( linkStringResource ) ) );
        startActivity( browserIntent );
    }

    private class AcknowledgmentsAdapter extends RecyclerView.Adapter<AcknowledgmentsAdapter.Holder>
    {
        class Holder extends RecyclerView.ViewHolder
        {
            final CardView Card;
            final FrameLayout Content;
            final TextView Title;
            final TextView Subtitle;

            public Holder( View itemView )
            {
                super( itemView );

                Card = (CardView) itemView;
                Content = (FrameLayout) itemView.findViewById( R.id.content );
                Title = (TextView) itemView.findViewById( R.id.title );
                Subtitle = (TextView) itemView.findViewById( R.id.subtitle );
            }
        }

        private final LayoutInflater mInflater;

        private AcknowledgmentsAdapter()
        {
            mInflater = LayoutInflater.from( AcknowledgmentsActivity.this );
        }

        @Override
        public Holder onCreateViewHolder( ViewGroup viewGroup, int viewType )
        {
            final View view = LayoutInflater
                    .from( AcknowledgmentsActivity.this )
                    .inflate( R.layout.acknowledgment_card_layout, viewGroup, false );
            return new Holder( view );
        }

        @Override
        public void onBindViewHolder( Holder viewHolder, int position )
        {
            viewHolder.Content.removeAllViews();

            switch( position )
            {
                case 0:
                {
                    mInflater.inflate( R.layout.flaticon_layout, viewHolder.Content );
                    viewHolder.Title.setText( "Flat Icon" );
                    viewHolder.Card.setOnClickListener( new View.OnClickListener()
                    {
                        @Override
                        public void onClick( View v )
                        {
                            browseTo( R.string.flaticon_link );
                        }
                    } );
                    break;
                }

                case 1:
                {
                    mInflater.inflate( R.layout.subtlepatterns_layout, viewHolder.Content );
                    final BitmapDrawable image = (BitmapDrawable) getResources().getDrawable( R.drawable.gplaypattern );
                    image.setTileModeXY( Shader.TileMode.REPEAT, Shader.TileMode.REPEAT );
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        viewHolder.Content.setBackground( image );
                    }
                    else
                    {
                        viewHolder.Content.setBackgroundDrawable( image );
                    }

                    viewHolder.Title.setText( "Subtle Patterns" );
                    viewHolder.Card.setOnClickListener( new View.OnClickListener()
                    {
                        @Override
                        public void onClick( View v )
                        {
                            browseTo( R.string.subtlepatterns_link );
                        }
                    } );
                    break;
                }

                case 2:
                {
                    viewHolder.Title.setText( "Vectorized Playing Cards" );
                    viewHolder.Card.setOnClickListener( new View.OnClickListener()
                    {
                        @Override
                        public void onClick( View v )
                        {
                            browseTo( R.string.cards_link );
                        }
                    } );
                    final CardDisplayLayout cardDisplayLayout = new CardDisplayLayout( AcknowledgmentsActivity.this )
                    {
                        @Override
                        public PlayingCardView createPlayingCardView( String cardHolderID, Card card )
                        {
                            final PlayingCardView cardView = new PlayingCardView( getContext(), cardHolderID, card, 0.5f )
                            {
                                @Override
                                protected void onAttachedToWindow()
                                {
                                    super.onAttachedToWindow();
                                    spreadCard();
                                }
                            };
                            cardView.flip( true, false );
                            return cardView;
                        }

                        @Override
                        public boolean onInterceptTouchEvent( MotionEvent event )
                        {
                            return true;
                        }

                        @Override
                        public boolean onTouchEvent( @NonNull MotionEvent event )
                        {
                            return false;
                        }
                    };

                    cardDisplayLayout.setCanVibrate( false );
                    cardDisplayLayout.setMinimumHeight( getResources().getDimensionPixelOffset( R.dimen.card_layout_min_height ) );
                    viewHolder.Content.addView( cardDisplayLayout );

                    final ArrayList< Card > cards = new ArrayList< Card >();
                    for( int i = 0; i < 20; i++ )
                    {
                        cards.add( new Card() );
                    }
                    cardDisplayLayout.getCardHolderListener().onCardsAdded( "owner_id", cards.toArray( new Card[ cards.size() ] ) );
                    break;
                }

                case 3:
                {
                    mInflater.inflate( R.layout.chunkfive_layout, viewHolder.Content );
                    viewHolder.Title.setText( "ChunkFive Font" );
                    viewHolder.Card.setOnClickListener( new View.OnClickListener()
                    {
                        @Override
                        public void onClick( View v )
                        {
                            browseTo( R.string.chunkFive_link );
                        }
                    } );
                    break;
                }

                case 4:
                {
                    final ViewGroup viewGroup = (ViewGroup) mInflater.inflate( R.layout.card_flip_animations_layout, viewHolder.Content );
                    viewHolder.Title.setText( "Card Flip Animations" );
                    viewHolder.Card.setOnClickListener( new View.OnClickListener()
                    {
                        @Override
                        public void onClick( View v )
                        {
                            browseTo( R.string.cardFlipAnimations_link );
                        }
                    } );
                    final CardDisplayLayout cardDisplayLayout = new CardDisplayLayout( AcknowledgmentsActivity.this )
                    {
                        @Override
                        public PlayingCardView createPlayingCardView( String cardHolderID, Card card )
                        {
                            return new PlayingCardView( getContext(), cardHolderID, card, 0, 0 )
                            {
                                @Override
                                protected void onAttachedToWindow()
                                {
                                    super.onAttachedToWindow();
                                    flip();
                                }

                                @Override
                                protected void onLayout( boolean changed, int left, int top, int right, int bottom )
                                {
                                    super.onLayout( changed, left, top, right, bottom );
                                    ViewGroup parent = (ViewGroup) getParent();
                                    if( parent != null )
                                    {
                                        setX( ( parent.getWidth() - getWidth() ) / 2.0f );
                                    }
                                }

                                @Override
                                public void flip( boolean faceUp, boolean animate )
                                {
                                    super.flip( faceUp, animate );

                                    mFromMiddle.setAnimationListener( new Animation.AnimationListener()
                                    {
                                        @Override
                                        public void onAnimationStart( Animation animation )
                                        {
                                        }

                                        @Override
                                        public void onAnimationEnd( Animation animation )
                                        {
                                            postDelayed( new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    flip();
                                                }
                                            }, 2000 );
                                        }

                                        @Override
                                        public void onAnimationRepeat( Animation animation )
                                        {
                                        }
                                    } );
                                }
                            };
                        }

                        @Override
                        public boolean onInterceptTouchEvent( MotionEvent event )
                        {
                            return true;
                        }

                        @Override
                        public boolean onTouchEvent( @NonNull MotionEvent event )
                        {
                            return false;
                        }
                    };

                    cardDisplayLayout.setCanVibrate( false );
                    cardDisplayLayout.setMinimumHeight( getResources().getDimensionPixelOffset( R.dimen.card_layout_min_height ) );
                    ( (ViewGroup) viewGroup.findViewById( R.id.cardDisplayFrame ) ).addView( cardDisplayLayout );
                    cardDisplayLayout.getCardHolderListener().onCardAdded( "owner_id", new Card( 12 ) );
                    break;
                }
            }
        }

        @Override
        public void onViewRecycled( Holder holder )
        {
            super.onViewRecycled( holder );

            switch( holder.getPosition() )
            {
                case 1:
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        holder.Content.setBackground( null );
                    }
                    else
                    {
                        holder.Content.setBackgroundDrawable( null );
                    }
                    break;
            }
        }

        @Override
        public int getItemCount()
        {
            return 5;
        }
    }
}
