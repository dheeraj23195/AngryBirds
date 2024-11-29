package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LevelsScreen implements Screen {
    private final AngryBirdsGame game;
    private Stage uiStage;
    private Skin uiSkin;
    public static List<LevelSingle> gameLevels = new ArrayList<>();
    public static List<Bird> birdTypes = new ArrayList<>();
    private static final float LEVEL_BTN_SIZE = 180f;
    private static final float NAV_BTN_SIZE = 80f;

    static {
        initializeGameData();
    }

    private static void initializeGameData() {
        birdTypes.clear();
        birdTypes.add(new Bird(1600, BirdType.RED, 1));
        birdTypes.add(new Bird(1200, BirdType.YELLOW, 3));
        birdTypes.add(new Bird(1400, BirdType.BLACK, 5));

        gameLevels.clear();
        for (int i = 1; i <= 6; i++) {
            boolean canPlay = (i == 1);
            int levelStars = 0;
            gameLevels.add(new LevelSingle(i, levelStars, canPlay));
        }
    }

    public static void unlockNextLevel(int currentLevel) {
        if (currentLevel < gameLevels.size()) {
            gameLevels.get(currentLevel).setUnlocked(true);
        }
    }

    public static List<Bird> getAvailableBirdsForLevel(int levelNumber) {
        List<Bird> levelBirds = new ArrayList<>();
        for (Bird bird : birdTypes) {
            if (bird.getUnlockLevel() <= levelNumber) {
                levelBirds.add(bird);
            }
        }
        return levelBirds;
    }


    public LevelsScreen(AngryBirdsGame game) {
        this.game = game;
        setupScreen();
    }

    private void setupScreen() {
        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        Texture bgImg = AssetManager.getInstance().getTexture("levelscreen_bg.jpg");
        Image bgImage = new Image(new TextureRegion(bgImg));
        bgImage.setFillParent(true);
        uiStage.addActor(bgImage);

        addNavButtons();
        createLevelGrid();
    }


    private void addNavButtons() {
        ImageButton backBtn = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(AssetManager.getInstance().getTexture("backbutton.png"))));
        backBtn.setSize(NAV_BTN_SIZE, NAV_BTN_SIZE);
        backBtn.setPosition(30, Gdx.graphics.getHeight() - NAV_BTN_SIZE - 30);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        uiStage.addActor(backBtn);

        ImageButton settingsBtn = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(AssetManager.getInstance().getTexture("gear_icon.png"))));
        settingsBtn.setSize(NAV_BTN_SIZE, NAV_BTN_SIZE);
        settingsBtn.setPosition(Gdx.graphics.getWidth() - NAV_BTN_SIZE - 30,
            Gdx.graphics.getHeight() - NAV_BTN_SIZE - 30);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, new LevelsScreen(game)));
            }
        });
        uiStage.addActor(settingsBtn);
    }

    private void createLevelGrid() {
        Table levelGrid = new Table();
        levelGrid.setFillParent(true);
        levelGrid.center();

        Table topRow = new Table();
        Table bottomRow = new Table();

        for (int i = 0; i < gameLevels.size(); i++) {
            Table levelBtn = makeLevelButton(gameLevels.get(i));
            if (i < 3) {
                topRow.add(levelBtn).size(LEVEL_BTN_SIZE).padRight(i < 2 ? 50 : 0);
            } else {
                bottomRow.add(levelBtn).size(LEVEL_BTN_SIZE).padRight(i < 5 ? 50 : 0);
            }
        }

        levelGrid.add(topRow).padBottom(50).row();
        levelGrid.add(bottomRow);
        uiStage.addActor(levelGrid);
    }

    private Table makeLevelButton(LevelSingle level) {
        Table btnContainer = new Table();
        btnContainer.setTransform(true);
        Stack btnStack = new Stack();

        Texture btnTexture;
        if (!level.isUnlocked()) {
            btnTexture = AssetManager.getInstance().getTexture("locked_button.png");
        } else {
            btnTexture = AssetManager.getInstance().getTexture("level_" + level.getRating() + "star.png");
        }

        float aspectRatio = (float)btnTexture.getWidth() / btnTexture.getHeight();
        float width = LEVEL_BTN_SIZE;
        float height = width / aspectRatio;

        Image btnImage = new Image(new TextureRegionDrawable(new TextureRegion(btnTexture)));
        btnImage.setSize(width, height);
        btnStack.add(btnImage);

        if (level.isUnlocked()) {
            Label.LabelStyle textStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
            textStyle.font = uiSkin.getFont("default-font");
            textStyle.fontColor = Color.WHITE;

            Label levelNum = new Label(String.valueOf(level.getNumber()), textStyle);
            levelNum.setFontScale(2.5f);
            levelNum.setAlignment(Align.center);

            Container<Label> textBox = new Container<>(levelNum);
            textBox.center().padBottom(75f);
            btnStack.add(textBox);

            btnContainer.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new GameScreen(game,level));
                }
            });
        }

        btnContainer.add(btnStack).size(width);
        return btnContainer;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
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
        if (uiSkin != null) {
            uiSkin.dispose();
        }
    }
}
