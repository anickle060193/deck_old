package com.adamnickle.deck;


import android.app.Activity;
import android.graphics.Color;
import android.view.MotionEvent;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Interfaces.CardHolderListener;

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

    @Override
    public synchronized void resetCard( String cardHolderID, Card card )
    {
    }

    @Override
    public synchronized void layoutCards( String cardHolderID )
    {
    }
}
