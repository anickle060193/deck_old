package com.adamnickle.deck;


import android.os.Bundle;

import dev.dworks.libs.awizard.WizardActivity;
import dev.dworks.libs.awizard.model.PageList;
import dev.dworks.libs.awizard.model.WizardModel;
import dev.dworks.libs.awizard.model.WizardModelCallbacks;
import dev.dworks.libs.awizard.model.page.BranchPage;
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
                return new PageList( new BranchPage( this, "Custom Game" )
                        .addBranch( "Go Fish" )
                        .addBranch(
                                "Custom",
                                new SingleFixedChoicePage( this, "Cards" )
                                        .setChoices( "Custom Choice 1", "Custom Choice 2" ),
                                new ReviewPage( this, "Custom Game" )
                        )
                        .setValue( "Go Fish" ) );
            }
        } );

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }
}
