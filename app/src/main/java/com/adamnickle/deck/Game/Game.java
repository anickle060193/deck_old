package com.adamnickle.deck.Game;


import android.app.Activity;

import com.adamnickle.deck.spi.GameConnectionInterface;
import com.adamnickle.deck.spi.GameConnectionListener;
import com.adamnickle.deck.spi.GameUiInterface;
import com.adamnickle.deck.spi.GameUiListener;

public abstract class Game implements GameConnectionListener, GameUiListener
{
    protected GameConnectionInterface mGameConnection;
    protected GameUiInterface mGameUI;
    protected Activity mParentActivity;

    public Game( Activity parentActivity, GameConnectionInterface gameConnectionInterface )
    {
        mParentActivity = parentActivity;
        mGameConnection = gameConnectionInterface;
    }

    public void setGameUiInterface( GameUiInterface gameUiInterface )
    {
        mGameUI = gameUiInterface;
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public abstract void onPlayerConnect( String deviceAddress, String deviceName );

    @Override
    public abstract void onPlayerDisconnect( String senderAddress );

    @Override
    public void onNotification( String notification )
    {
        if( mGameUI != null )
        {
            mGameUI.displayNotification( notification );
        }
    }

    @Override
    public abstract void onConnectionStateChange( int newState );

    @Override
    public abstract void onCardReceive( String senderAddress, Card card );

    @Override
    public abstract void onCardSendRequested( String requesterAddress );

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public abstract boolean onAttemptSendCard( Card card );
}
