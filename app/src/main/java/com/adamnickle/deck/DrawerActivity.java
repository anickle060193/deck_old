package com.adamnickle.deck;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;


public abstract class DrawerActivity extends Activity
{
    protected DrawerLayout mDrawerLayout;
    private FrameLayout mContent;
    private ListView mDrawer;

    @Override
    public void setContentView( int layoutResID )
    {
        setContentView( getLayoutInflater().inflate( layoutResID, null, true ) );
    }

    @Override
    public void setContentView( View view )
    {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate( R.layout.activity_drawer, null, false  );
        mContent = (FrameLayout) mDrawerLayout.findViewById( R.id.content );
        mDrawer = (ListView) mDrawerLayout.findViewById( R.id.drawer );
        mContent.ad
    }
}
