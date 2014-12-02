package com.adamnickle.deck;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class AdFragment extends Fragment
{
    private AdView mAdView;

    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.ad_layout, container, false );
    }

    @Override
    public void onActivityCreated( @Nullable Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        mAdView = (AdView) getActivity().findViewById( R.id.adView );
        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice( "545CC0CAE1AD071668562D44E507933F" )
                .build();
        mAdView.loadAd( adRequest );
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mAdView.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        mAdView.resume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mAdView.destroy();
    }
}
