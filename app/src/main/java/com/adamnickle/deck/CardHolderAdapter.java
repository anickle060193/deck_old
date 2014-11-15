package com.adamnickle.deck;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adamnickle.deck.Game.CardHolder;
import com.adamnickle.deck.Interfaces.GameConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class CardHolderAdapter extends BaseAdapter
{
    private static final int ROW_RESOURCE = android.R.layout.simple_list_item_1;

    private final Context mContext;
    private final ArrayList<CardHolder> mData;

    public CardHolderAdapter( Context context, CardHolder[] cardHolders )
    {
        this( context, Arrays.asList( cardHolders ) );
    }

    public CardHolderAdapter( Context context, Collection< CardHolder > cardHolders )
    {
        mContext = context;
        if( cardHolders instanceof ArrayList )
        {
            mData = (ArrayList< CardHolder >) cardHolders;
        }
        else
        {
            mData = new ArrayList< CardHolder >( cardHolders );
        }
        Collections.sort( mData, CardHolder.CardHolderComparator.getInstance() );
    }

    public Context getContext()
    {
        return mContext;
    }

    @Override
    public int getCount()
    {
        return mData.size();
    }

    @Override
    public CardHolder getItem( int position )
    {
        return mData.get( position );
    }

    @Override
    public long getItemId( int position )
    {
        return (long) getItem( position ).hashCode();
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        final View row;
        final Holder holder;

        if( convertView == null )
        {
            row = LayoutInflater.from( getContext() ).inflate( ROW_RESOURCE, parent, false );
            holder = new Holder();
            holder.Text = (TextView) row.findViewById( android.R.id.text1 );
            row.setTag( holder );
        }
        else
        {
            row = convertView;
            holder = (Holder) row.getTag();
        }

        final CardHolder cardHolder = getItem( position );
        final SpannableString ss = getCardHolderDisplayString( cardHolder );
        holder.Text.setText( ss );

        return row;
    }

    private SpannableString getCardHolderDisplayString( CardHolder cardHolder )
    {
        final CharacterStyle style;

        final String cardHolderID = cardHolder.getID();
        if( cardHolderID.startsWith( TableFragment.TABLE_ID ) )
        {
            style = new StyleSpan( Typeface.BOLD );
        }
        else if( cardHolderID.startsWith( TableFragment.DRAW_PILE_ID_PREFIX ) )
        {
            style = new ForegroundColorSpan( getContext().getResources().getColor( R.color.DrawPileBlue ) );
        }
        else if( cardHolderID.startsWith( TableFragment.DISCARD_PILE_ID_PREFIX ) )
        {
            style = new ForegroundColorSpan( getContext().getResources().getColor( R.color.DiscardPileRed ) );
        }
        else if( cardHolderID.equals( GameConnection.MOCK_SERVER_ADDRESS ) )
        {
            style = new StyleSpan( Typeface.BOLD );
        }
        else
        {
            style = new StyleSpan( Typeface.NORMAL );
        }

        final SpannableString ss = new SpannableString( cardHolder.getName() );
        ss.setSpan( style, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
        return ss;
    }

    class Holder
    {
        TextView Text;
    }
}
