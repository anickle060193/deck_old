package com.adamnickle.deck;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.adamnickle.deck.Game.CardHolder;

import java.util.Collection;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DialogHelper
{
    public static abstract class OnEditTextDialogClickListener
    {
        public void onPositiveButtonClick( DialogInterface dialogInterface, String text ) { }
        public void onNegativeButtonClick( DialogInterface dialogInterface ) { }
    }

    public static AlertDialog.Builder createEditTextDialog( Context context, String title, String positiveButtonText, String negativeButtonText, final OnEditTextDialogClickListener onClickListener )
    {
        final EditText editText = (EditText) LayoutInflater.from( context ).inflate( R.layout.dialog_edit_text, null );

        return DialogHelper
                .createBlankAlertDialog( context, title )
                .setPositiveButton( positiveButtonText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        onClickListener.onPositiveButtonClick( dialogInterface, editText.getText().toString() );
                    }
                } )
                .setNegativeButton( negativeButtonText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        onClickListener.onNegativeButtonClick( dialogInterface );
                    }
                } )
                .setView( editText );
    }

    public static AlertDialog.Builder createBlankAlertDialog( Context context, String title )
    {
        return new AlertDialog.Builder( context )
                .setTitle( title );
    }

    public static AlertDialog.Builder createSelectItemDialog( Context context, String title, Object[] items, DialogInterface.OnClickListener listener )
    {
        return DialogHelper
                .createBlankAlertDialog( context, title )
                .setAdapter( new ArrayAdapter<Object>( context, android.R.layout.simple_list_item_1, items ), listener );
    }

    public static void showPopup( Context context, String title, String message, String buttonText )
    {
        DialogHelper
                .createBlankAlertDialog( context, title )
                .setMessage( message )
                .setPositiveButton( buttonText, null )
                .show();
    }

    public static void displayNotification( final Activity activity, final String notification, final Style style )
    {
        if( activity != null )
        {
            activity.runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    Crouton.makeText( activity, notification, style ).show();
                }
            } );
        }
    }

    public static AlertDialog.Builder displayCardHolderList( Context context, String title, Collection< CardHolder > cardHolders, final CardHolderOnClickListener onClickListener )
    {
        final CardHolderAdapter adapter = new CardHolderAdapter( context, cardHolders );
        return DialogHelper
                .createBlankAlertDialog( context, title )
                .setAdapter( adapter, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        if( onClickListener != null )
                        {
                            onClickListener.onClick( dialog, adapter.getItem( which ) );
                        }
                    }
                } );
    }

    public static interface CardHolderOnClickListener
    {
        public void onClick( DialogInterface dialog, CardHolder cardHolder );
    }
}
