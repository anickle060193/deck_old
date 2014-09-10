package com.adamnickle.deck.Game;

import android.app.Activity;

import com.adamnickle.deck.Interfaces.ConnectionInterfaceFragment;
import com.adamnickle.deck.Interfaces.GameConnectionInterface;

import java.security.InvalidParameterException;
import java.util.HashMap;


public class ServerGame extends Game
{
    private final HashMap<String, Player> mDisconnectedPlayers;
    private final CardCollection mDeck;

    private final Player mLocalPlayer;
    private int mCanSendCard;

    public ServerGame( Activity parentActivity, GameConnectionInterface gameConnectionInterface )
    {
        super( parentActivity, gameConnectionInterface );

        mDisconnectedPlayers = new HashMap< String, Player >();
        mDeck = new CardCollection();
        mLocalPlayer = new Player( mGameConnection.getDefaultLocalPlayerID(), mGameConnection.getDefaultLocalPlayerName() );
        mPlayers.put( mLocalPlayer.getID(), mLocalPlayer );
        mCanSendCard = 0;
    }

    private void requestCardFromPlayer( String ID )
    {
        if( ID.equals( mLocalPlayer.getID() ) )
        {
            mCanSendCard++;
        }
        else
        {
            mGameConnection.requestCard( ID );
        }
    }

    private void sendCardToPlayer( String ID, Card card )
    {
        if( ID.equals( mLocalPlayer.getID() ) )
        {
            mLocalPlayer.addCard( card );
        }
        else
        {
            mGameConnection.sendCard( ID, card );
        }
    }

    @Override
    public void clearPlayerHands()
    {
        for( Player player : mPlayers.values() )
        {
            if( player == mLocalPlayer )
            {
                mLocalPlayer.clearHand();
            }
            else
            {
                mGameConnection.clearPlayerHand( player.getID() );
            }
        }
    }

    @Override
    public Card[] getCards()
    {
        return mDeck.getCards();
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
                if( mGameUI != null )
                {
                    mGameUI.displayNotification( "Waiting for more players..." );
                }
                break;

            case ConnectionInterfaceFragment.STATE_CONNECTED_LISTENING:
                break;

            case ConnectionInterfaceFragment.STATE_CONNECTING:
                throw new InvalidParameterException( "ServerGames should not receive state change to \"STATE_CONNECTING\"" );
        }
    }

    @Override
    public void onPlayerConnect( String deviceID, String deviceName )
    {
        final Player player = mDisconnectedPlayers.remove( deviceID );
        if( player == null )
        {
            mPlayers.put( deviceID, new Player( deviceID, deviceName ) );
            mGameUI.displayNotification( "Player connected: " + deviceName + " - " + deviceID );
        }
        else
        {
            mPlayers.put( player.getID(), player );
            mGameUI.displayNotification( "Player reconnected: " + player.getName() + " - " + player.getID() );
            for( Card card : player.getAllCards() )
            {
                mGameConnection.sendCard( player.getID(), card );
            }
        }
    }

    @Override
    public void onPlayerDisconnect( String playerID )
    {
        Player removedPlayer = mPlayers.remove( playerID );
        if( removedPlayer != null )
        {
            mDisconnectedPlayers.put( removedPlayer.getID(), removedPlayer );
            mGameUI.displayNotification( removedPlayer.getName() + " has disconnected." );
        }
    }

    @Override
    public void onCardReceive( String senderID, Card card )
    {
        mDeck.addCard( card );
    }

    @Override
    public void onCardSendRequested( String requesterID )
    {
        // Why is player asking server for card?
    }

    @Override
    public void onReceivePlayerName( String senderID, String name )
    {
        mPlayers.get( senderID ).setName( name );
    }

    @Override
    public void onClearPlayerHand( String senderID )
    {
        // Why is player asking server to clear hand?
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( Card card )
    {
        if( mCanSendCard > 0 )
        {
            this.onCardReceive( mLocalPlayer.getID(), card );
            mCanSendCard--;
            return true;
        }
        else
        {
            return false;
        }
    }
}
