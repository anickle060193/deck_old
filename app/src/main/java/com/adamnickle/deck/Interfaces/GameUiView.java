package com.adamnickle.deck.Interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;

import com.adamnickle.deck.CardDrawable;
import com.adamnickle.deck.Game.Card;


public abstract class GameUiView extends View
{
    public GameUiView( Context context )
    {
        super( context );
    }

    public abstract void setGameUiListener( GameUiListener gameUiListener );
    public abstract void setGameGestureListener( GameGestureListener gameGestureListener );
    public abstract void addCardDrawable( Card card );
    public abstract boolean removeCardDrawable( Card card );
    public abstract void removeAllCardDrawables();
    public abstract void resetCard( Card card );
    public abstract void sortCards( int sortType );
    public abstract void layoutCards();
    public abstract AlertDialog.Builder createEditTextDialog( String title, String preSetText, String positiveButtonText, String negativeButtonText, OnEditTextDialogClickListener onClickListener  );
    public abstract AlertDialog.Builder createSelectItemDialog( String title, Object items[], DialogInterface.OnClickListener listener );
    public abstract void showPopup( String title, String message );
    public abstract void displayNotification( String notification );

    public static abstract class OnEditTextDialogClickListener
    {
        public void onPositiveButtonClick( DialogInterface dialogInterface, String text ) { }
        public void onNegativeButtonClick( DialogInterface dialogInterface ) { }
    }

    public abstract class GameGestureListener
    {
        public void onCardFling( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable ) { }
        public void onCardMove(  MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable ) { }
        public void onCardSingleTap( MotionEvent event, CardDrawable cardDrawable ) { }
        public void onCardDoubleTap( MotionEvent event, CardDrawable cardDrawable ) { }
        public void onGameDoubleTap( MotionEvent event ) { }
    }
}
