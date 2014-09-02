package com.adamnickle.deck.Game;

public class ClientGame extends Game
{
    private static final int INVALID_SERVER_ID = -1;

    private final Player mPlayer;
    private int mServerID;
    private String mServerName;
    private int mCanSendCard;

    public ClientGame()
    {
        mPlayer = new Player( 1, "My Name" );
        mCanSendCard = 0;
    }

    /*******************************************************************
     * GameConnectionListener Methods
     *******************************************************************/
    @Override
    public void onPlayerConnect( int deviceID, String deviceName )
    {
        mServerID = deviceID;
        mServerName = deviceName;
        mGameUI.displayNotification( "Connected to game server: " + mServerName + "." );
    }

    @Override
    public void onPlayerDisconnect( int deviceID )
    {
        if( deviceID == mServerID )
        {
            mServerID = INVALID_SERVER_ID;
            mServerName = "";
            mGameUI.displayNotification( "Disconnected from game server: " + mServerName + "." );
        }
    }

    @Override
    public void onCardReceive( int senderID, Card card )
    {
        if( senderID == mServerID )
        {
            mPlayer.addCard( card );
            mGameUI.addCard( card );
        }
    }

    @Override
    public void onCardSendRequested( int requesterID )
    {
        if( requesterID == mServerID )
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
