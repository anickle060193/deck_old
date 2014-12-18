package com.adamnickle.deck;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Deck;
import com.adamnickle.deck.Game.Game;

import java.util.ArrayList;

import dev.dworks.libs.awizard.WizardActivity;
import dev.dworks.libs.awizard.model.PageList;
import dev.dworks.libs.awizard.model.ReviewItem;
import dev.dworks.libs.awizard.model.WizardModel;
import dev.dworks.libs.awizard.model.WizardModelCallbacks;
import dev.dworks.libs.awizard.model.page.BranchPage;
import dev.dworks.libs.awizard.model.page.MultipleFixedChoicePage;
import dev.dworks.libs.awizard.model.page.Page;
import dev.dworks.libs.awizard.model.page.ReviewPage;
import dev.dworks.libs.awizard.model.page.SingleFixedChoicePage;

public class GameCreatorWizardActivity extends WizardActivity
{
    public static final String EXTRA_GAME = "game";

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setWizardModel( new WizardModel( this )
        {
            @Override
            protected PageList onNewRootPageList()
            {
                /*
                final MultipleFixedChoicePage customDeckSetupPage = new MultipleFixedChoicePage( this, "Custom Deck Setup" );
                final String[] cardNames = new String[ Deck.CARD_COUNT ];
                for( int i = 0; i < Deck.CARD_COUNT; i++ )
                {
                    cardNames[ i ] = new Card( i ).toShortString();
                }
                customDeckSetupPage.setChoices( cardNames );

                return new PageList( new BranchPage( this, "Choose Game" )
                        .addBranch( "Go Fish" )
                        .addBranch( "Custom",
                                new CardPilesPage( this, "Card Piles Setup" )
                                        .setRequired( true ),
                                new BranchPage( this, "Card Deck Setup" )
                                        .addBranch( "All 52 cards" )
                                        .addBranch( "Custom Deck Setup", customDeckSetupPage )
                                        .setValue( "All 52 cards" )
                                        .setRequired( true ),
                                new ReviewPage( this, "Custom Game" ) )
                        .setValue( "Go Fish" )
                        .setRequired( true ) );
                */

                return new PageList(
                        new CardPilesPage( this, "Card Piles Setup" )
                                .setRequired( true ),
                        new ReviewPage( this, "Game Setup Review " ) );
            }
        } );

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    @Override
    public void onConfirmClick()
    {
        handleWizardDone();
    }

    @Override
    public void onDoneClick()
    {
        handleWizardDone();
    }

    private void handleWizardDone()
    {
        /*
        Page page1 = getWizardModel().findByKey( "Choose Game" );
        CardPilesPage page2 = (CardPilesPage) getWizardModel().findByKey( "Custom:Card Piles Setup" );
        Page page3 = getWizardModel().findByKey( "Custom:Card Deck Setup" );
        Page page4 = getWizardModel().findByKey( "Custom Deck Setup:Custom Deck Setup" );

        final String gameType = page1.getData().getString( Page.SIMPLE_DATA_KEY );
        final int drawPiles = page2.getDrawPiles();
        final int discardPiles = page2.getDiscardPiles();
        final String cardDeckSetup = page3.getData().getString( Page.SIMPLE_DATA_KEY );
        final ArrayList<String> cards = (ArrayList<String>) page4.getData().get( Page.SIMPLE_DATA_KEY );
        */

        final CardPilesPage page = (CardPilesPage) onGetPage( "Card Piles Setup" );
        final int drawPiles = page.getDrawPiles();
        final int discardPiles = page.getDiscardPiles();

        final Game game = new Game();
        game.DrawPiles = drawPiles;
        game.DiscardPiles = discardPiles;

        final Intent result = new Intent();
        result.putExtra( GameCreatorWizardActivity.EXTRA_GAME, game );
        setResult( Activity.RESULT_OK, result );
        finish();
    }
}
