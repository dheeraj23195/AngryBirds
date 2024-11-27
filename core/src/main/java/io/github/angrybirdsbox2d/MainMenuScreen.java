package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;

public class MainMenuScreen implements Screen {
    private final AngryBirdsGame game;
    private Stage uiStage;
    private Skin uiSkin;
    private static final float NAV_BTN_SIZE = 80f;

    public MainMenuScreen(AngryBirdsGame game) {
        this.game = game;
        setupMainMenu();
    }

    private void setupMainMenu() {
        uiStage = new Stage(new ScreenViewport());
        setupBackground();
        addSettingsButton();
        createMenuButtons();
    }

    private void setupBackground() {
        Texture bgImg = AssetManager.getInstance().getTexture("main_screen_bg.jpg");
        Image bg = new Image(new TextureRegion(bgImg));
        bg.setFillParent(true);
        uiStage.addActor(bg);
    }

    private void addSettingsButton() {
        Texture gearImg = AssetManager.getInstance().getTexture("gear_icon.png");
        ImageButton settingsBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(gearImg)));
        settingsBtn.setSize(NAV_BTN_SIZE, NAV_BTN_SIZE);
        settingsBtn.setPosition(Gdx.graphics.getWidth() - NAV_BTN_SIZE - 30,
            Gdx.graphics.getHeight() - NAV_BTN_SIZE - 30);

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, new MainMenuScreen(game)));
            }
        });
        uiStage.addActor(settingsBtn);
    }

    private void createMenuButtons() {
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        Table btnTable = new Table();
        btnTable.setFillParent(true);
        uiStage.addActor(btnTable);

        TextButton.TextButtonStyle btnStyle = createButtonStyle();
        TextButton playBtn = new TextButton("Play", btnStyle);
        TextButton loadBtn = new TextButton("Load Game", btnStyle);
        TextButton exitBtn = new TextButton("Exit", btnStyle);

        setupButtonPadding(playBtn, loadBtn, exitBtn);
        addButtonListeners(playBtn, loadBtn, exitBtn);

        btnTable.bottom().padBottom(50);
        btnTable.add(playBtn).width(300).height(80).padRight(20);
        btnTable.add(loadBtn).width(300).height(80).padRight(20);
        btnTable.add(exitBtn).width(300).height(80);
    }

    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(uiSkin.get(TextButton.TextButtonStyle.class));
        Texture btnBg = AssetManager.getInstance().getTexture("button_bg.png");
        style.up = new TextureRegionDrawable(new TextureRegion(btnBg));
        style.down = style.up;
        style.over = style.up;
        style.font = uiSkin.getFont("default-font");
        style.fontColor = Color.WHITE;
        return style;
    }

    private void setupButtonPadding(TextButton... buttons) {
        for(TextButton btn : buttons) {
            btn.getLabelCell().pad(10);
        }
    }

    private void addButtonListeners(TextButton playBtn, TextButton loadBtn, TextButton exitBtn) {
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelsScreen(game));
            }
        });

        loadBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SavedGamesScreen(game, false, MainMenuScreen.this));
            }
        });

        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        uiSkin.dispose();
    }
}
