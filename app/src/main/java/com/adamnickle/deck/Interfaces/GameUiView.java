package com.adamnickle.deck.Interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;

import com.adamnickle.deck.CardDrawable;
import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardCollection;


public abstract class GameUiView extends View
{
    public GameUiView( Context context )
    {
        super( context );
    }

    public abstract void setGameUiListener( GameUiListener gameUiListener );
    public abstract void setGameGestureListener( GameGestureListener gameGestureListener );
    public abstract CardHolderListener getPlayerListener();
    public abstract void resetCard( String cardHolderID, Card card );
    public abstract void sortCards( String cardHolderID, CardCollection.SortingType sortingType );
    public abstract void layoutCards( String cardHolderID );
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
        public boolean onCardFling( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float velocityX, float velocityY )
        {
            return true;
        }

        public boolean onCardMove( MotionEvent e1, MotionEvent e2, CardDrawable cardDrawable, float x, float y )
        {
            return true;
        }

        public boolean onCardSingleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            return true;
        }

        public boolean onCardDoubleTap( MotionEvent event, CardDrawable cardDrawable )
        {
            return true;
        }

        public boolean onGameDoubleTap( MotionEvent event )
        {
            return true;
        }
    }
}
