package com.adamnickle.deck;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Deck;

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
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setWizardModel( new WizardModel( this )
        {
            @Override
            protected PageList onNewRootPageList()
            {
                final MultipleFixedChoicePage customDeckSetupPage = new MultipleFixedChoicePage( this, "Custom Deck Setup" );
                final String[] cardNames = new String[ Deck.CARD_COUNT ];
                for( int i = 0; i < Deck.CARD_COUNT; i++ )
                {
                    cardNames[ i ] = new Card( i ).toShortString();
                }
                customDeckSetupPage.setChoices( cardNames );

                return new PageList( new BranchPage( this, "Choose Game" )
                        .addBranch( "Go Fish" )
                        .addBranch(
                                "Custom",
                                new CardPilesPage( this, "Card Piles Setup" ),
                                new BranchPage( this, "Card Deck Setup" )
                                        .addBranch( "All 52 cards" )
                                        .addBranch( "Custom Deck Setup", customDeckSetupPage )
                                        .setValue( "All 52 cards" ),
                                new ReviewPage( this, "Custom Game" )
                        )
                        .setValue( "Go Fish" ) );
            }
        } );

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }
}
