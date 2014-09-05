package com.adamnickle.deck.Game;

import android.app.Activity;

import com.adamnickle.deck.spi.ConnectionInterfaceFragment;
import com.adamnickle.deck.spi.GameConnectionInterface;

import java.security.InvalidParameterException;

public class ClientGame extends Game
{
    private final Player mPlayer;
    private String mServerAddress;
    private String mServerName;
    private int mCanSendCard;

    public ClientGame( Activity parentActivity, GameConnectionInterface gameConnectionInterface )
    {
        super( parentActivity, gameConnectionInterface );
        mPlayer = new Player( "", "My Name" );
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
    public void onPlayerConnect( String deviceAddress, String deviceName )
    {
        mServerAddress = deviceAddress;
        mServerName = deviceName;
        mGameUI.displayNotification( "Connected to game server: " + mServerName + " - " + mServerAddress );
    }

    @Override
    public void onPlayerDisconnect( String senderAddress )
    {
        if( senderAddress == mServerAddress )
        {
            mGameUI.displayNotification( "Disconnected from game server: " + mServerName + " - " + mServerAddress );
            mServerAddress = "";
            mServerName = "";
        }
    }

    @Override
    public void onCardReceive( String senderAddress, Card card )
    {
        if( senderAddress == mServerAddress )
        {
            mPlayer.addCard( card );
            mGameUI.addCard( card );
        }
    }

    @Override
    public void onCardSendRequested( String requesterAddress )
    {
        if( requesterAddress == mServerAddress )
        {
            mCanSendCard++;
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
            mGameConnection.sendCard( mServerAddress, card );
            mCanSendCard--;
            return true;
        }
        else
        {
            return false;
        }
    }
}
