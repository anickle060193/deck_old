package com.adamnickle.deck.Interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.adamnickle.deck.Game.Card;


public abstract class GameUiInterfaceView extends View
{
    public GameUiInterfaceView( Context context )
    {
        super( context );
    }

    public abstract void setGameUiListener( GameUiListener gameUiListener );
    public abstract void addCardDrawable( Card card );
    public abstract boolean removeCardDrawable( Card card );
    public abstract void resetCard( Card card );
    public abstract AlertDialog.Builder createSelectItemDialog( String title, Object items[], DialogInterface.OnClickListener listener );
    public abstract void displayNotification( String notification );
}
