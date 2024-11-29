package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SettingsScreen implements Screen {
    private final AngryBirdsGame game;
    private Stage uiStage;
    private Stage bgStage;
    private Skin uiSkin;
    private final Screen lastScreen;
    private final ShapeRenderer dimmer;

    private static final float SETTINGS_WIDTH = 400f;
    private static final float SETTINGS_HEIGHT = 300f;
    private static final float SLIDER_WIDTH = 200f;

    public SettingsScreen(AngryBirdsGame game, Screen lastScreen) {
        this.game = game;
        this.lastScreen = lastScreen;
        this.dimmer = new ShapeRenderer();
        setupUI();
    }

    private void setupUI() {
        bgStage = new Stage(new ScreenViewport());
        uiStage = new Stage(new ScreenViewport());
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        createSettingsWindow();
    }

    private void createSettingsWindow() {
        Table settingsPanel = new Table();
        settingsPanel.setBackground(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("button_bg.png"))));
        settingsPanel.center();

        createHeader(settingsPanel);
        createMusicControls(settingsPanel);
        setupWindowPosition(settingsPanel);
        uiStage.addActor(settingsPanel);
    }

    private void createHeader(Table panel) {
        Label.LabelStyle titleStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        titleStyle.font = uiSkin.getFont("default-font");
        titleStyle.fontColor = Color.WHITE;
        Label titleText = new Label("Settings", titleStyle);
        titleText.setFontScale(1.5f);

        ImageButton exitBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("close.png"))));
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleExit();
            }
        });

        Table headerBar = new Table();
        headerBar.add(titleText).expandX();
        headerBar.add(exitBtn).size(40, 40).padRight(10);

        panel.add(headerBar).width(SETTINGS_WIDTH).padLeft(60).padBottom(15).row();
    }

    private void createMusicControls(Table panel) {
        Label musicLabel = new Label("Music Volume:", uiSkin);
        musicLabel.setColor(Color.WHITE);
        panel.add(musicLabel).padLeft(25).padBottom(5).row();

        Slider musicSlider = new Slider(0f, 1f, 0.1f, false, uiSkin);
        musicSlider.setValue(AudioManager.getInstance().getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().setMusicVolume(musicSlider.getValue());
            }
        });
        panel.add(musicSlider).width(SLIDER_WIDTH).padBottom(20).row();
    }


    private void setupWindowPosition(Table panel) {
        panel.setSize(SETTINGS_WIDTH, SETTINGS_HEIGHT);
        panel.setPosition(
            (Gdx.graphics.getWidth() - SETTINGS_WIDTH) / 2,
            (Gdx.graphics.getHeight() - SETTINGS_HEIGHT) / 2
        );
    }

    private void handleExit() {
        if (lastScreen instanceof GameScreen) {
            GameScreen gameScreen = (GameScreen) lastScreen;
            game.setScreen(gameScreen, false);
            if (!gameScreen.isPaused()) {
                gameScreen.togglePause();
            }
            Gdx.input.setInputProcessor(gameScreen.getPauseMenu().getStage());
        } else {
            game.setScreen(lastScreen);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float delta) {
        lastScreen.render(delta);
        dimBackground();
        uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        uiStage.draw();
    }

    private void dimBackground() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        dimmer.begin(ShapeRenderer.ShapeType.Filled);
        dimmer.setColor(0, 0, 0, 0.7f);
        dimmer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        dimmer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
        bgStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        if (lastScreen != null) {
            lastScreen.pause();
        }
    }

    @Override
    public void resume() {
        if (lastScreen != null) {
            lastScreen.resume();
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        bgStage.dispose();
        uiSkin.dispose();
        dimmer.dispose();
    }
}
