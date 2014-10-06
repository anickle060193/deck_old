package com.adamnickle.deck;


import android.app.Activity;
import android.graphics.Color;

public class TableView extends GameView
{
    public TableView( Activity activity )
    {
        super( activity );
        setBackgroundColor( Color.parseColor( "#66CCFF" ) );
    }

    @Override
    protected void setGameBackground( int drawableIndex )
    {
    }
}
