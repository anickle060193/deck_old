package com.adamnickle.deck.Game;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import com.adamnickle.deck.spi.ConnectionInterfaceFragment;
import com.adamnickle.deck.spi.GameConnectionInterface;

import java.security.InvalidParameterException;
import java.util.HashMap;


public class ServerGame extends Game
{
    private static final String LOCAL_PLAYER_ADDRESS = BluetoothAdapter.getDefaultAdapter().getAddress();

    private final HashMap<String, Player> mPlayers;
    private final HashMap<String, Player> mDisconnectedPlayers;
    private final CardCollection mDeck;

    private final Player mLocalPlayer;
    private int mCanSendCard;

    public ServerGame( Activity parentActivity, GameConnectionInterface gameConnectionInterface )
    {
        super( parentActivity, gameConnectionInterface );
        mPlayers = new HashMap< String, Player >();
        mDisconnectedPlayers = new HashMap< String, Player >();
        mDeck = new CardCollection();
        mLocalPlayer = new Player( LOCAL_PLAYER_ADDRESS, "Local Player" );
        mPlayers.put( mLocalPlayer.getAddress(), mLocalPlayer );
        mCanSendCard = 0;
    }

    private void requestCardFromPlayer( String address )
    {
        if( address.equals( mLocalPlayer.getAddress() ) )
        {
            mCanSendCard++;
        }
        else
        {
            mGameConnection.requestCard( address );
        }
    }

    private void sendCardToPlayer( String address, Card card )
    {
        if( address.equals( mLocalPlayer.getAddress() ) )
        {
            mLocalPlayer.addCard( card );
        }
        else
        {
            mGameConnection.sendCard( address, card );
        }
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
    public void onPlayerConnect( String deviceAddress, String deviceName )
    {
        final Player player = mDisconnectedPlayers.remove( deviceAddress );
        if( player == null )
        {
            mPlayers.put( deviceAddress, new Player( deviceAddress, deviceName ) );
            mGameUI.displayNotification( "Player connected: " + deviceName + " - " + deviceAddress );
        }
        else
        {
            mPlayers.put( player.getAddress(), player );
            mGameUI.displayNotification( "Player reconnected: " + player.getName() + " - " + player.getAddress() );
            for( Card card : player.getAllCards() )
            {
                mGameConnection.sendCard( player.getAddress(), card );
            }
        }
    }

    @Override
    public void onPlayerDisconnect( String senderAddress )
    {
        Player removedPlayer = mPlayers.get( senderAddress );
        if( removedPlayer != null )
        {
            mDisconnectedPlayers.put( removedPlayer.getAddress(), removedPlayer );
            mGameUI.displayNotification( removedPlayer.getName() + " has disconnected." );
        }
    }

    @Override
    public void onCardReceive( String senderAddress, Card card )
    {
        mDeck.addCard( card );
    }

    @Override
    public void onCardSendRequested( String requesterAddress )
    {
        // Why is player asking server for card?
    }

    /*******************************************************************
     * GameUiListener Methods
     *******************************************************************/
    @Override
    public boolean onAttemptSendCard( Card card )
    {
        if( mCanSendCard > 0 )
        {
            this.onCardReceive( mLocalPlayer.getAddress(), card );
            mCanSendCard--;
            return true;
        }
        else
        {
            return false;
        }
    }
}
