package com.adamnickle.deck.Game;


import java.util.ArrayList;

public final class PresetGames
{
    private PresetGames() { }

    private static final ArrayList<Game> mPresetGames = new ArrayList<>();

    public static ArrayList<Game> getGames()
    {
        return (ArrayList<Game>) mPresetGames.clone();
    }

    static
    {
        Game fish = new Game();
        fish.GameName = "Go Fish";
        fish.DiscardPiles = 0;
        fish.DrawPiles = 1;
        mPresetGames.add( fish );

        Game war = new Game();
        war.GameName = "War";
        war.DrawPiles = 0;
        war.DiscardPiles = 0;
        mPresetGames.add( war );

        Game hearts = new Game();
        hearts.GameName = "Hearts";
        hearts.DiscardPiles = 0;
        hearts.DrawPiles = 0;
        mPresetGames.add( hearts );
    }
}
