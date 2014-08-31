package com.adamnickle.deck.Game;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Game
{
    public static final Random RANDOM = new Random();

    private ArrayList< Player > mPlayers;
    private ArrayList<Card> mDeck;
    private Card.CardComparator mCardComparator;

    public Game( int players )
    {
        mPlayers = new ArrayList< Player >( players );
        for( int i = 0; i < players; i++ )
        {
            mPlayers.add( new Player( "" ) );
        }
        mDeck = new ArrayList< Card >( Deck.CARD_COUNT );
        for( int i = 0; i < Deck.CARD_COUNT; i++ )
        {
            mDeck.add( new Card( i ) );
        }
        mCardComparator = new Card.CardComparator( CardCollection.SORT_BY_RANK );
    }

    public void shuffle()
    {
        for( int i = 0; i < mDeck.size(); i++ )
        {
            final int index = RANDOM.nextInt( mDeck.size() );
            final Card card = mDeck.get( index );
            mDeck.set( index, mDeck.get( i ) );
            mDeck.set( i, card );
        }
    }

    public void dealCards( int cardPerPlayer )
    {
        for( int i = 0; i < cardPerPlayer; i++ )
        {
            for( Player player : mPlayers )
            {
                if( mDeck.size() == 0 )
                {
                    return;
                }
                else
                {
                    player.addCard( mDeck.remove( 0 ) );
                }
            }
        }
    }

    public void setupGame()
    {
        shuffle();
        dealCards( Deck.CARD_COUNT / mPlayers.size() );
    }

    public void playGame()
    {
        Log.d( "GAME", "STARTED GAME" );
        Card playedCards[] = new Card[ mPlayers.size() ];
        int rounds = 1;
        while( stillPlaying() )
        {
            Log.d( "GAME", "+-------- ROUND " + rounds + " --------+" );
            for( int i = 0; i < mPlayers.size(); i++ )
            {
                playedCards[ i ] = mPlayers.get( i ).playCard();
                if( playedCards[ i ] != null )
                {
                    Log.d( "GAME", "Player " + i + " played " + playedCards[ i ].toString() );
                }
            }

            int highestCardIndex = 0;
            for( int i = 1; i < playedCards.length; i++ )
            {
                if( playedCards[ i ] != null )
                {
                    if( mCardComparator.compare( playedCards[ highestCardIndex ], playedCards[ i ] ) < 0 )
                    {
                        highestCardIndex = i;
                    }
                }
            }

            mPlayers.get( highestCardIndex ).addCards( Arrays.asList( playedCards ) );
            for( int i = 0; i < playedCards.length; i++ )
            {
                playedCards[ i ] = null;
            }
            rounds++;
            SystemClock.sleep( 1000 );
        }
    }

    private boolean stillPlaying()
    {
        boolean stillPlaying = false;
        for( Player player : mPlayers )
        {
            if( player.isStillPlaying() )
            {
                if( stillPlaying )
                {
                    return true;
                }
                else
                {
                    stillPlaying = true;
                }
            }
        }
        return false;
    }
}
