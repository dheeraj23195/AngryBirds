package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AngryBirdsGame extends Game {
    public SpriteBatch gameBatch;
    private MainMenuScreen menuScreen;

    @Override
    public void create() {
        gameBatch = new SpriteBatch();
        AssetManager.getInstance().loadTextures();
        menuScreen = new MainMenuScreen(this);
        setScreen(menuScreen);
        AudioManager.getInstance().playBackgroundMusic();
    }

    public MainMenuScreen getMenuScreen() {
        return menuScreen;
    }

    public void setScreen(Screen newScreen, boolean shouldDispose) {
        if (this.screen != null && shouldDispose) {
            this.screen.hide();
            this.screen.dispose();
        }
        this.screen = newScreen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void setScreen(Screen newScreen) {
        setScreen(newScreen, true);
    }

    @Override
    public void dispose() {
        gameBatch.dispose();
        AudioManager.getInstance().dispose();
    }
}
