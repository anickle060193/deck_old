package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.adamnickle.deck.Game.ClientGameConnection;
import com.adamnickle.deck.Game.ServerGameConnection;
import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;

import java.security.InvalidParameterException;


public class GameActivity extends Activity
{
    private ConnectionFragment mConnectionFragment;
    private GameConnection mGameConnection;
    private GameFragment mGameFragment;
    private TableFragment mTableFragment;
    private SlidingFrameLayout mTableView;
    private DrawerLayout mDrawerLayout;
    private DrawingFragment mDrawingFragment;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.activity_game );

        mTableView = (SlidingFrameLayout) findViewById( R.id.table );
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawerLayout );
        mDrawerLayout.setScrimColor( Color.TRANSPARENT );
        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawerOpen, R.string.drawerClosed )
        {
            @Override
            public void onDrawerOpened( View drawerView )
            {
                super.onDrawerOpened( drawerView );
                mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_OPEN );
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed( View drawerView )
            {
                super.onDrawerClosed( drawerView );
                mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED );
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener( mDrawerToggle );


        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final ConnectionFragment.ConnectionType connectionType = (ConnectionFragment.ConnectionType) intent.getSerializableExtra( ConnectionFragment.EXTRA_CONNECTION_TYPE );
            final String className = intent.getStringExtra( ConnectionFragment.EXTRA_CONNECTION_CLASS_NAME );
            try
            {
                mConnectionFragment = (ConnectionFragment) Class.forName( className ).newInstance();
            }
            catch( ClassCastException e )
            {
                throw new ClassCastException( className + " does not extend " + ConnectionFragment.class.getSimpleName() );
            }
            catch( InstantiationException e )
            {
                e.printStackTrace();
            }
            catch( IllegalAccessException e )
            {
                e.printStackTrace();
            }
            catch( ClassNotFoundException e )
            {
                e.printStackTrace();
            }

            mConnectionFragment.setConnectionType( connectionType );

            getFragmentManager()
                    .beginTransaction()
                    .add( mConnectionFragment, ConnectionFragment.class.getName() )
                    .commit();

            mGameFragment = new GameFragment();
            mTableFragment = new TableFragment();

            mGameConnection = null;
            switch( connectionType )
            {
                case CLIENT:
                    mGameConnection = new ClientGameConnection( mConnectionFragment );
                    break;

                case SERVER:
                    mGameConnection = new ServerGameConnection( mConnectionFragment );
                    break;

                default:
                    throw new InvalidParameterException( "Invalid connection type: " + connectionType );
            }
            mConnectionFragment.setConnectionListener( mGameConnection );

            mGameConnection.addGameConnectionListener( mGameFragment );
            mGameConnection.addGameConnectionListener( mTableFragment );

            mGameFragment.setGameConnection( mGameConnection );
            mTableFragment.setGameConnection( mGameConnection );

            mDrawingFragment = new DrawingFragment();

            getFragmentManager()
                    .beginTransaction()
                    .replace( R.id.table, mTableFragment, TableFragment.class.getName() )
                    .replace( R.id.game, mGameFragment, GameFragment.class.getName() )
                    .replace( R.id.drawingPanel, mDrawingFragment, DrawingFragment.class.getName() )
                    .commit();
        }
        else
        {
            final FragmentManager fragmentManager = getFragmentManager();
            mTableFragment = (TableFragment) fragmentManager.findFragmentByTag( TableFragment.class.getName() );
            mGameFragment = (GameFragment) fragmentManager.findFragmentByTag( GameFragment.class.getName() );
            mDrawingFragment = (DrawingFragment) fragmentManager.findFragmentByTag( DrawingFragment.class.getName() );
            mConnectionFragment = (ConnectionFragment) fragmentManager.findFragmentByTag( ConnectionFragment.class.getName() );
        }
    }

    @Override
    public void invalidateOptionsMenu()
    {
        if( mDrawerLayout.isDrawerOpen( GravityCompat.END ) )
        {
            mTableFragment.setHasOptionsMenu( false );
            mGameFragment.setHasOptionsMenu( false );
            mConnectionFragment.setHasOptionsMenu( false );

            mDrawingFragment.setHasOptionsMenu( true );
        }
        else
        {
            mTableFragment.setHasOptionsMenu( true );
            mGameFragment.setHasOptionsMenu( true );
            mConnectionFragment.setHasOptionsMenu( true );

            mDrawingFragment.setHasOptionsMenu( false );
        }
        super.invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.actionToggleTable:
                mDrawerLayout.closeDrawer( GravityCompat.END );
                mTableView.toggleState();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public void onBackPressed()
    {
        if( mTableView.isOpen() )
        {
            mTableView.collapseFrame();
            return;
        }

        if( mConnectionFragment != null && mConnectionFragment.isConnected() )
        {
            String message = null;
            switch( mConnectionFragment.getConnectionType() )
            {
                case CLIENT:
                    message = "Close current Game? This will disconnect you from server.";
                    break;

                case SERVER:
                    message = "Close current Game? This will disconnect all players.";
                    break;
            }

            new AlertDialog.Builder( this )
                    .setTitle( "Close Game" )
                    .setMessage( message )
                    .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            GameActivity.this.finish();
                        }
                    } )
                    .setNegativeButton( "Cancel", null )
                    .show();
        }
        else
        {
            GameActivity.this.finish();
        }
    }
}
