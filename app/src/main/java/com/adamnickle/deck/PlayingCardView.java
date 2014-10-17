package com.adamnickle.deck;


import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Interfaces.GameUiListener;
import com.squareup.picasso.Picasso;

public class PlayingCardView extends ImageView
{
    private final GameUiListener mListener;
    private final Card mCard;
    private String mOwnerID;

    public PlayingCardView( Context context, GameUiListener gameUiListener, String ownerID, Card card )
    {
        super( context );

        mListener = gameUiListener;
        mCard = card;
        mOwnerID = ownerID;

        this.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );

        Picasso.with( getContext() ).load( mCard.getResource() ).into( this );
    }
}
