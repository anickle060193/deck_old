package com.adamnickle.deck;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    public static AlertDialog.Builder createEditTextDialog( Context context, String title, String preSetText, String positiveButtonText, String negativeButtonText, final OnEditTextDialogClickListener onClickListener )
    {
        final EditText editText = new EditText( context );

        AlertDialog.Builder builder = new AlertDialog.Builder( context )
                .setTitle( title )
                .setView( editText );

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialogInterface, int i )
            {
                switch( i )
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        onClickListener.onPositiveButtonClick( dialogInterface, editText.getText().toString() );
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        onClickListener.onNegativeButtonClick( dialogInterface );
                        break;
                }
            }
        };

        if( positiveButtonText != null && !positiveButtonText.isEmpty())
        {
            builder.setPositiveButton( positiveButtonText, clickListener);
        }

        if( negativeButtonText != null && !negativeButtonText.isEmpty() )
        {
            builder.setNegativeButton( negativeButtonText, clickListener);
        }

        return builder;
    }

    public static AlertDialog.Builder createBlankAlertDialog( Context context, String title )
    {
        return new AlertDialog.Builder( context )
                .setTitle( title );
    }

    public static AlertDialog.Builder createSelectItemDialog( Context context, String title, Object[] items, DialogInterface.OnClickListener listener )
    {
        String[] itemNames;
        if( items instanceof String[] )
        {
            itemNames = (String[]) items;
        }
        else
        {
            itemNames = new String[ items.length ];
            for( int i = 0; i < items.length; i++ )
            {
                itemNames[ i ] = items[ i ].toString();
            }
        }
        return new AlertDialog.Builder( context )
                .setTitle( title )
                .setItems( itemNames, listener );
    }

    public static void showPopup( Context context, String title, String message )
    {
        new AlertDialog.Builder( context )
                .setTitle( title )
                .setMessage( message )
                .setPositiveButton( "OK", null )
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
