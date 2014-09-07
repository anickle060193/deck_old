package com.adamnickle.deck.Game;

import android.app.Activity;

import com.adamnickle.deck.spi.ConnectionInterfaceFragment;
import com.adamnickle.deck.spi.GameConnectionInterface;

import java.security.InvalidParameterException;

public class ClientGame extends Game
{
    private final Player mPlayer;
    private String mServerID;
    private String mServerName;
    private int mCanSendCard;

    public ClientGame( Activity parentActivity, GameConnectionInterface gameConnectionInterface )
    {
        super( parentActivity, gameConnectionInterface );

        mPlayer = new Player( mGameConnection.getDefaultLocalPlayerID(), mGameConnection.getDefaultLocalPlayerName() );
        mPlayers.put( mPlayer.getID(), mPlayer );
        mCanSendCard = 0;
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public void onConnectionStateChange( int newState )
    {
        switch( newState )
        {
            case ConnectionInterfaceFragment.STATE_NONE:
                break;

            case ConnectionInterfaceFragment.STATE_LISTENING:
                throw new InvalidParameterException( "ClientGame should never enter state \"STATE_LISTENING\"" );

            case ConnectionInterfaceFragment.STATE_CONNECTED_LISTENING:
                throw new InvalidParameterException( "ClientGame should never enter state \"STATE_CONNECTED_LISTENING\"" );

            case ConnectionInterfaceFragment.STATE_CONNECTING:
                if( mGameUI != null )
                {
                    mGameUI.displayNotification( "Connecting..." );
                }
                break;
        }
    }

    @Override
    public void onPlayerConnect( String deviceID, String deviceName )
    {
        mServerID = deviceID;
        mServerName = deviceName;
        mGameUI.displayNotification( "Connected to game server: " + mServerName + " - " + mServerID );
    }

    @Override
    public void onPlayerDisconnect( String playerID )
    {
        if( playerID.equals( mServerID ) )
        {
            mGameUI.displayNotification( "Disconnected from game server: " + mServerName + " - " + mServerID );
            mServerID = "";
            mServerName = "";
        }
    }

    @Override
    public void onCardReceive( String senderID, Card card )
    {
        if( senderID.equals( mServerID ) )
        {
            mPlayer.addCard( card );
            mGameUI.addCard( card );
        }
    }

    @Override
    public void onCardSendRequested( String requesterID )
    {
        if( requesterID.equals( mServerID ) )
        {
            mCanSendCard++;
        }
    }

    @Override
    public void onReceivePlayerName( String senderID, String name )
    {
        if( senderID.equals( mServerID ) )
        {
            mServerName = name;
        }
    }

    @Override
    public void onClearPlayerHand( String senderID )
    {
        if( senderID.equals( mServerID ) )
        {
            mPlayer.clearHand();
        }
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( Card card )
    {
        if( mCanSendCard > 0 )
        {
            mGameConnection.sendCard( mServerID, card );
            mCanSendCard--;
            return true;
        }
        else
        {
            return false;
        }
    }
}
