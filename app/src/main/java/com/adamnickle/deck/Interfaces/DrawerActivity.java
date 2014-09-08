package com.adamnickle.deck.Interfaces;

import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
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
    private ListView mDrawer;

    public void setDrawerArrayAdapter( ArrayAdapter<?> drawerArrayAdapter )
    {
        mDrawer.setAdapter( drawerArrayAdapter );
    }

    public void setDrawerOnClickListener( View.OnClickListener onClickListener )
    {
        mDrawer.setOnClickListener( onClickListener );
    }

    @Override
    public void setContentView( int layoutResID )
    {
        setContentView( getLayoutInflater().inflate( layoutResID, null, true ) );
    }

    @Override
    public void setContentView( View view )
    {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate( R.layout.activity_drawer, null, false );
        mDrawerLayout.setDrawerListener( mDrawerToggleListener );
        mContent = (FrameLayout) mDrawerLayout.findViewById( R.id.content );
        mDrawer = (ListView) mDrawerLayout.findViewById( R.id.drawer );
        mContent.addView( view );
    }

    private ActionBarDrawerToggle mDrawerToggleListener = new ActionBarDrawerToggle( this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed )
    {
        @Override
        public void onDrawerClosed( View view )
        {
            super.onDrawerClosed( view );
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerOpened( View view )
        {
            super.onDrawerOpened( view );
            invalidateOptionsMenu();
        }
    };

    public class OnDrawerClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick( AdapterView<?> adapterView, View view, int index, long l )
        {
            ListView drawer = (ListView)view;
            drawer.setItemChecked( index, true );
            ( (DrawerLayout) view.getParent() ).closeDrawer( view );
        }
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen( mDrawer );
        return super.onPrepareOptionsMenu( menu );
    }
}
