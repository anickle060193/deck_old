package com.adamnickle.deck;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;


public class GameDrawerLayout extends DrawerLayout
{
    public GameDrawerLayout( Context context )
    {
        super( context );
    }

    public GameDrawerLayout( Context context, AttributeSet attrs )
    {
        super( context, attrs );
    }

    public GameDrawerLayout( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if( KeyEvent.KEYCODE_BACK == keyCode && ( this.isDrawerOpen( GravityCompat.START ) || this.isDrawerOpen( GravityCompat.END ) ) )
        {
            KeyEventCompat.startTracking( event );
            return true;
        }
        return super.onKeyDown( keyCode, event );
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if( KeyEvent.KEYCODE_BACK == keyCode && !event.isCanceled() && ( this.isDrawerOpen( GravityCompat.START ) || this.isDrawerOpen( GravityCompat.END ) ) )
        {
            this.closeDrawers();
            return true;
        }
        return super.onKeyUp( keyCode, event );
    }
}
