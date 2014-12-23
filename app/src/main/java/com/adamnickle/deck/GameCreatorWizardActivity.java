package com.adamnickle.deck;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.PresetGames;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Style;
import dev.dworks.libs.awizard.WizardActivity;
import dev.dworks.libs.awizard.model.PageList;
import dev.dworks.libs.awizard.model.WizardModel;
import dev.dworks.libs.awizard.model.page.BranchPage;
import dev.dworks.libs.awizard.model.page.Page;
import dev.dworks.libs.awizard.model.page.ReviewPage;

public class GameCreatorWizardActivity extends WizardActivity
{
    public static final String EXTRA_GAME = "game";

    private ArrayList<Game> mGames;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        mGames = PresetGames.getGames();
        mGames.addAll( Game.getSavedCustomGames( GameCreatorWizardActivity.this ) );

        setWizardModel( new WizardModel( this )
        {
            @Override
            protected PageList onNewRootPageList()
            {
                final ReviewPage reviewPage = new ReviewPage( this, "Game Setup Review" );

                final BranchPage gameSelection = new BranchPage( this, "Choose Game" );
                gameSelection.setRequired( true );

                for( Game game : mGames )
                {
                    gameSelection.addBranch( game.GameName );
                }

                gameSelection.addBranch(
                        "Custom Game",
                        new GameSetupPage( this, "Card Piles Setup" )
                                .setRequired( true ) );

                return new PageList(
                        gameSelection,
                        reviewPage );
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
        final Page gamePage = onGetPage( "Choose Game" );
        final GameSetupPage gameSetupPage = (GameSetupPage) onGetPage( "Custom Game:Card Piles Setup" );
        final String selectedGame = gamePage.getData().getString( Page.SIMPLE_DATA_KEY );

        if( selectedGame.startsWith( "Custom" ) )
        {
            final Game game = new Game();
            game.GameName = gameSetupPage.getGameName();
            game.DrawPiles = gameSetupPage.getDrawPiles();
            game.DiscardPiles = gameSetupPage.getDiscardPiles();

            final Intent result = new Intent();
            result.putExtra( GameCreatorWizardActivity.EXTRA_GAME, game );
            setResult( Activity.RESULT_OK, result );

            DialogHelper
                    .createBlankAlertDialog( this, "Save Game?" )
                    .setMessage( "Would you like to save this Custom Game to be played later?" )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            if( Game.saveGame( GameCreatorWizardActivity.this, game ) )
                            {
                                DialogHelper
                                        .displayNotification(
                                                GameCreatorWizardActivity.this,
                                                "Custom Game Save Successful",
                                                Style.CONFIRM );
                            }
                            else
                            {
                                DialogHelper
                                        .displayNotification(
                                                GameCreatorWizardActivity.this,
                                                "Custom Game Save Unsuccessful",
                                                Style.ALERT );
                            }
                            finish();
                        }
                    } )
                    .setNegativeButton( "No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            finish();
                        }
                    } )
                    .show();
        }
        else
        {
            Game ret = null;
            for( Game game : mGames )
            {
                if( game.GameName.equals( selectedGame ) )
                {
                    ret = game;
                }
            }
            final Intent result = new Intent();
            result.putExtra( GameCreatorWizardActivity.EXTRA_GAME, ret );
            setResult( Activity.RESULT_OK, result );
            finish();
        }
    }
}
