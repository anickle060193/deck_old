package com.adamnickle.deck.Interfaces;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.adamnickle.deck.R;


public abstract class DrawerActivity extends Activity
{
    protected DrawerLayout mDrawerLayout;
    private FrameLayout mContent;
    private ListView mNavDrawer;
    private ListView mActionDrawer;
    private ActionBarDrawerToggle mDrawerToggleListener;
    private ArrayAdapter mNavDrawerAdapter;
    private ArrayAdapter mActionDrawerAdapter;
    private DrawerOnClickListener mNavDrawerOnClickListener;
    private DrawerOnClickListener mActionDrawerOnClickListener;
    private boolean mUsingNavDrawer;
    private boolean mUsingActionDrawer;
    private DrawerActionListener mDrawerActionListener;

    public DrawerActivity()
    {
        mUsingNavDrawer = false;
        mUsingActionDrawer = false;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        super.setContentView( R.layout.activity_drawer );
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        mNavDrawer = (ListView) findViewById( R.id.nav_drawer );
        mActionDrawer = (ListView) findViewById( R.id.action_drawer );

        if( !mUsingNavDrawer )
        {
            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mNavDrawer );
        }

        if( !mUsingActionDrawer )
        {
            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mActionDrawer );
        }

        mContent = (FrameLayout) findViewById( R.id.content );

        mDrawerToggleListener = new ActionBarDrawerToggle( this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed )
        {
            @Override
            public boolean onOptionsItemSelected( MenuItem item )
            {
                if( item.getItemId() == android.R.id.home && mDrawerLayout.isDrawerOpen( mActionDrawer ) )
                {
                    mDrawerLayout.closeDrawer( mActionDrawer );
                    return true;
                }
                else
                {
                    return mUsingNavDrawer && super.onOptionsItemSelected( item );
                }
            }

            @Override
            public void onDrawerClosed( View view )
            {
                super.onDrawerClosed( view );
                invalidateOptionsMenu();
                if( view == mNavDrawer )
                {
                    if( mDrawerActionListener != null )
                    {
                        mDrawerActionListener.onNavDrawerClosed( (ListView) view );
                    }
                }
                else if( view == mActionDrawer )
                {
                    if( mDrawerActionListener != null )
                    {
                        mDrawerActionListener.onActionDrawerClosed( (ListView) view );
                    }
                }
            }

            @Override
            public void onDrawerOpened( View view )
            {
                super.onDrawerOpened( view );
                invalidateOptionsMenu();
                if( view == mNavDrawer )
                {
                    if( mDrawerActionListener != null )
                    {
                        mDrawerActionListener.onNavDrawerOpen( (ListView) view );
                    }
                }
                else if( view == mActionDrawer )
                {
                    if( mDrawerActionListener != null )
                    {
                        mDrawerActionListener.onActionDrawerOpen( (ListView) view );
                    }
                }
            }
        };

        mDrawerLayout.setDrawerListener( mDrawerToggleListener );
        if( mNavDrawerAdapter != null )
        {
            mNavDrawer.setAdapter( mNavDrawerAdapter );
        }
        if( mNavDrawerOnClickListener != null )
        {
            mNavDrawer.setOnItemClickListener( mNavDrawerOnClickListener );
        }
        if( mActionDrawerAdapter != null )
        {
            mActionDrawer.setAdapter( mActionDrawerAdapter );
        }
        if( mActionDrawerOnClickListener != null )
        {
            mActionDrawer.setOnItemClickListener( mActionDrawerOnClickListener );
        }

        if( mUsingNavDrawer )
        {
            ActionBar actionBar = getActionBar();
            if( actionBar != null )
            {
                actionBar.setDisplayHomeAsUpEnabled( true );
                if( Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH )
                {
                    actionBar.setHomeButtonEnabled( true );
                }
            }
        }
    }

    public void initializeNavDrawer( ArrayAdapter navDrawerAdapter, DrawerOnClickListener navOnClickListener )
    {
        mUsingNavDrawer = true;
        mNavDrawerAdapter = navDrawerAdapter;
        mNavDrawerOnClickListener = navOnClickListener;

        if( mNavDrawer != null )
        {
            mNavDrawer.setAdapter( mNavDrawerAdapter );
            mNavDrawer.setOnItemClickListener( mNavDrawerOnClickListener );
            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED, mNavDrawer );
        }

        ActionBar actionBar = getActionBar();
        if( actionBar != null )
        {
            actionBar.setDisplayHomeAsUpEnabled( true );
            if( Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH )
            {
                actionBar.setHomeButtonEnabled( true );
            }
        }
    }

    public void initializeActionDrawer( ArrayAdapter actionDrawerArrayAdapter, DrawerOnClickListener actionDrawerOnClickListener )
    {
        mUsingActionDrawer = true;
        mActionDrawerAdapter = actionDrawerArrayAdapter;
        mActionDrawerOnClickListener = actionDrawerOnClickListener;

        if( mActionDrawer != null )
        {
            mActionDrawer.setAdapter( mActionDrawerAdapter );
            mActionDrawer.setOnItemClickListener( actionDrawerOnClickListener );
            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED, mActionDrawer );
        }
    }

    public void setDrawerActionListener( DrawerActionListener listener )
    {
        mDrawerActionListener = listener;
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState )
    {
        super.onPostCreate( savedInstanceState );
        mDrawerToggleListener.syncState();
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );
        mDrawerToggleListener.onConfigurationChanged( newConfig );
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        boolean navDrawerOpen = mDrawerLayout.isDrawerOpen( mNavDrawer );
        boolean actionDrawerOpen = mDrawerLayout.isDrawerOpen( mActionDrawer );
        return super.onPrepareOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        if( mDrawerToggleListener.onOptionsItemSelected( item ) )
        {
            return true;
        }
        else
        {
            return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public void setContentView( int layoutResID )
    {
        getLayoutInflater().inflate( layoutResID, mContent, true );
    }

    @Override
    public void setContentView( View view )
    {
        mContent.removeAllViews();
        mContent.addView( view );
    }

    public static class DrawerOnClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick( AdapterView<?> adapterView, View view, int index, long l )
        {
            ListView drawer = (ListView)adapterView;
            drawer.setItemChecked( index, true );
            ( (DrawerLayout) drawer.getParent() ).closeDrawer( drawer );
        }
    }

    public interface DrawerActionListener
    {
        public void onNavDrawerOpen( ListView navDrawer );
        public void onNavDrawerClosed( ListView navDrawer );
        public void onActionDrawerOpen( ListView actionDrawer);
        public void onActionDrawerClosed( ListView actionDrawer );
    }
}
