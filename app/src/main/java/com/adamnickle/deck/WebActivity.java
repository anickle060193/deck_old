package com.adamnickle.deck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebActivity extends ActionBarActivity
{
    public static final String EXTRA_URL = "extra_url";

    private WebView mWebView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.webview_layout );

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        setTitle( "Loading..." );

        final View progressBar = findViewById( R.id.loading );

        mWebView = (WebView) findViewById( R.id.webview );
        mWebView.setWebViewClient( new WebViewClient()
        {
            @Override
            public void onPageFinished( WebView view, String url )
            {
                WebActivity.this.setTitle( mWebView.getTitle() );
                progressBar.setVisibility( View.GONE );
            }
        } );

        final Intent intent = getIntent();
        if( intent != null )
        {
            final String url = intent.getStringExtra( EXTRA_URL );
            if( url != null )
            {
                mWebView.loadUrl( url );
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if( mWebView.canGoBack() )
        {
            mWebView.goBack();
        }
        else
        {
            super.onBackPressed();
        }
    }

    public static void showPage( Activity activity, String url )
    {
        Intent intent = new Intent( activity, WebActivity.class );
        intent.putExtra( EXTRA_URL, url );
        activity.startActivity( intent );
    }

    public static void showPage( Activity activity, @StringRes int urlResource )
    {
        showPage( activity, activity.getString( urlResource ) );
    }
}
