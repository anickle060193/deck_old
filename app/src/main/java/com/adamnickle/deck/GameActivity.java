package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.adamnickle.deck.Game.ClientGameConnection;
import com.adamnickle.deck.Game.ServerGameConnection;
import com.adamnickle.deck.Interfaces.ConnectionFragment;
import com.adamnickle.deck.Interfaces.GameConnection;

import java.security.InvalidParameterException;

import de.keyboardsurfer.android.widget.crouton.Crouton;


public class GameActivity extends ActionBarActivity
{
    public final static int REQUEST_START_GAME = 1;

    public final static int RESULT_DISCONNECTED_FROM_SERVER = Activity.RESULT_FIRST_USER;
    public final static int RESULT_INVALID_VERSIONS = RESULT_DISCONNECTED_FROM_SERVER + 1;

    private ConnectionFragment mConnectionFragment;
    private GameConnection mGameConnection;
    private GameFragment mGameFragment;
    private TableFragment mTableFragment;
    private SlidingFrameLayout mTableView;
    private DrawerLayout mDrawerLayout;
    private ScratchPadFragment mDrawingFragment;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );


        getSupportActionBar().setDisplayShowTitleEnabled( false );
        getSupportActionBar().setIcon( R.drawable.ic_launcher );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        mTableView = (SlidingFrameLayout) findViewById( R.id.table );
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawerLayout );
        mDrawerLayout.setScrimColor( Color.TRANSPARENT );
        mDrawerLayout.setDrawerListener( new ActionBarDrawerToggle( this, mDrawerLayout, R.string.drawerOpen, R.string.drawerClosed )
        {
            @Override
            public void onDrawerSlide( View drawerView, float slideOffset )
            {
                if( mTableView.isOpen() )
                {
                    mTableView.collapseFrame();
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onDrawerOpened( View drawerView )
            {
                mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.END );
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed( View drawerView )
            {
                mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END );
                invalidateOptionsMenu();
            }
        } );

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

            getSupportFragmentManager()
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

            mDrawingFragment = new ScratchPadFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace( R.id.table, mTableFragment, TableFragment.class.getName() )
                    .replace( R.id.game, mGameFragment, GameFragment.class.getName() )
                    .replace( R.id.drawingPanel, mDrawingFragment, ScratchPadFragment.class.getName() )
                    .commit();
        }
        else
        {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            mTableFragment = (TableFragment) fragmentManager.findFragmentByTag( TableFragment.class.getName() );
            mGameFragment = (GameFragment) fragmentManager.findFragmentByTag( GameFragment.class.getName() );
            mDrawingFragment = (ScratchPadFragment) fragmentManager.findFragmentByTag( ScratchPadFragment.class.getName() );
            mConnectionFragment = (ConnectionFragment) fragmentManager.findFragmentByTag( ConnectionFragment.class.getName() );
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Crouton.clearCroutonsForActivity( this );
    }

    @Override
    public void invalidateOptionsMenu()
    {
        if( mDrawerLayout.isDrawerOpen( Gravity.END ) )
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

            case android.R.id.home:
                showGameCloseConfirmation();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public void onBackPressed()
    {
        if( mDrawerLayout.isDrawerOpen( GravityCompat.END ) )
        {
            mDrawerLayout.closeDrawer( GravityCompat.END );
        }
        else if( mTableView.isOpen() )
        {
            mTableView.collapseFrame();
        }
        else
        {
            showGameCloseConfirmation();
        }
    }

    private void showGameCloseConfirmation()
    {
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
                            setResult( Activity.RESULT_CANCELED, new Intent( GameActivity.class.getName() ) );
                            GameActivity.this.finish();
                        }
                    } )
                    .setNegativeButton( "Cancel", null )
                    .show();
        }
        else
        {
            setResult( Activity.RESULT_CANCELED, new Intent( GameActivity.class.getName() ) );
            GameActivity.this.finish();
        }
    }
}
