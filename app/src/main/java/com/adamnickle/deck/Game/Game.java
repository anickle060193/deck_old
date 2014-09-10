package com.adamnickle.deck.Game;


import android.app.Activity;

import com.adamnickle.deck.Interfaces.GameConnectionInterface;
import com.adamnickle.deck.Interfaces.GameConnectionListener;
import com.adamnickle.deck.Interfaces.GameUiInterface;
import com.adamnickle.deck.Interfaces.GameUiListener;

import java.util.HashMap;
import java.util.Set;

public abstract class Game implements GameConnectionListener, GameUiListener
{
    protected GameConnectionInterface mGameConnection;
    protected GameUiInterface mGameUI;
    protected Activity mParentActivity;

    protected HashMap<String, Player> mPlayers;

    public Game( Activity parentActivity, GameConnectionInterface gameConnectionInterface )
    {
        mParentActivity = parentActivity;
        mGameConnection = gameConnectionInterface;

        mPlayers = new HashMap< String, Player >();
    }

    public void setGameUiInterface( GameUiInterface gameUiInterface )
    {
        mGameUI = gameUiInterface;
    }

    public void clearPlayerHands()
    {
        throw new UnsupportedOperationException( "clearPlayerHands cannot be called on this class" );
    }

    public int getPlayerCount()
    {
        return mPlayers.size();
    }

    public String[] getPlayerNames()
    {
        final Player players[] = mPlayers.values().toArray( new Player[ mPlayers.size() ] );
        final String names[] = new String[ players.length ];
        for( int i = 0; i < players.length; i++ )
        {
            names[ i ] = players[ i ].getName();
        }
        return names;
    }

    public String[] getPlayerIDs()
    {
        Set< String > keys = mPlayers.keySet();
        return keys.toArray( new String[ keys.size() ] );
    }

    public abstract Card[] getCards();

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public abstract void onPlayerConnect( String deviceID, String deviceName );

    @Override
    public abstract void onPlayerDisconnect( String playerID );

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
    public abstract void onCardReceive( String senderID, Card card );

    @Override
    public abstract void onCardSendRequested( String requesterID );

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public abstract boolean onAttemptSendCard( Card card );
}
