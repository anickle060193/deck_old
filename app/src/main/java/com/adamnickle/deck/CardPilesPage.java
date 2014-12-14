package com.adamnickle.deck;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.ArrayList;

import dev.dworks.libs.awizard.model.PageFragmentCallbacks;
import dev.dworks.libs.awizard.model.ReviewItem;
import dev.dworks.libs.awizard.model.WizardModelCallbacks;
import dev.dworks.libs.awizard.model.page.CustomerInfoPage;
import dev.dworks.libs.awizard.model.page.Page;


public class CardPilesPage extends Page
{
    public static final String DRAW_PILES_KEY = "draw_piles_key";
    public static final String DISCARD_PILES_KEY = "discard_piles_key";

    protected CardPilesPage( WizardModelCallbacks callbacks, String title )
    {
        super( callbacks, title );
    }

    @Override
    public Fragment createFragment()
    {
        return CardPilesFragment.create( getKey() );
    }

    @Override
    public void getReviewItems( ArrayList<ReviewItem> destination )
    {
        destination.add( new ReviewItem( "Draw Piles", mData.getString( DRAW_PILES_KEY ), getKey(), -1 ) );
        destination.add( new ReviewItem( "Discard Piles", mData.getString( DISCARD_PILES_KEY ), getKey(), -1 ) );
    }

    public static class CardPilesFragment extends Fragment
    {
        private static final String ARG_KEY = "key";

        private PageFragmentCallbacks mCallbacks;
        private String mKey;
        private CardPilesPage mPage;
        private TextView mDrawPiles;
        private TextView mDiscardPiles;

        public static CardPilesFragment create( String key )
        {
            Bundle args = new Bundle();
            args.putString( ARG_KEY, key );

            CardPilesFragment fragment = new CardPilesFragment();
            fragment.setArguments( args );
            return fragment;
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );

            Bundle args = getArguments();
            mKey = args.getString( ARG_KEY );
            mPage = (CardPilesPage) mCallbacks.onGetPage( mKey );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            View rootView = inflater.inflate( R.layout.fragment_page_card_piles, container, false );
            ( (TextView) rootView.findViewById( android.R.id.title ) ).setText( mPage.getTitle() );

            mDrawPiles = ( (TextView) rootView.findViewById( R.id.drawPiles ) );
            mDrawPiles.setText( mPage.getData().getString( CardPilesPage.DRAW_PILES_KEY ) );

            mDiscardPiles = ( (TextView) rootView.findViewById( R.id.discardPiles ) );
            mDiscardPiles.setText( mPage.getData().getString( CardPilesPage.DISCARD_PILES_KEY ) );
            return rootView;
        }

        @Override
        public void onAttach( Activity activity )
        {
            super.onAttach( activity );

            if( !( activity instanceof PageFragmentCallbacks ) )
            {
                throw new ClassCastException( "Activity must implement PageFragmentCallbacks" );
            }

            mCallbacks = (PageFragmentCallbacks) activity;
        }

        @Override
        public void onDetach()
        {
            super.onDetach();
            mCallbacks = null;
        }

        @Override
        public void onViewCreated( View view, Bundle savedInstanceState )
        {
            super.onViewCreated( view, savedInstanceState );

            mDrawPiles.addTextChangedListener( new TextWatcher()
            {
                @Override
                public void beforeTextChanged( CharSequence charSequence, int i, int i1, int i2 )
                {
                }

                @Override
                public void onTextChanged( CharSequence charSequence, int i, int i1, int i2 )
                {
                }

                @Override
                public void afterTextChanged( Editable editable )
                {
                    mPage.getData().putString( CardPilesPage.DRAW_PILES_KEY, ( editable != null ) ? editable.toString() : null );
                    mPage.notifyDataChanged();
                }
            } );

            mDiscardPiles.addTextChangedListener( new TextWatcher()
            {
                @Override
                public void beforeTextChanged( CharSequence charSequence, int i, int i1, int i2 )
                {
                }

                @Override
                public void onTextChanged( CharSequence charSequence, int i, int i1, int i2 )
                {
                }

                @Override
                public void afterTextChanged( Editable editable )
                {
                    mPage.getData().putString( CardPilesPage.DISCARD_PILES_KEY, ( editable != null ) ? editable.toString() : null );
                    mPage.notifyDataChanged();
                }
            } );
        }

        @Override
        public void setUserVisibleHint( boolean isVisibleToUser )
        {
            super.setUserVisibleHint( isVisibleToUser );

            if( mDrawPiles != null )
            {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService( Context.INPUT_METHOD_SERVICE );
                if( !isVisibleToUser )
                {
                    imm.hideSoftInputFromWindow( getView().getWindowToken(), 0 );
                }
            }
        }
    }
}
