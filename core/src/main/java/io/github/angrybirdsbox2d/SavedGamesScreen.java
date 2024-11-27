package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SavedGamesScreen implements Screen {
    private final AngryBirdsGame game;
    private Stage uiStage;
    private Skin uiSkin;
    private final boolean saveMode;
    private final Screen lastScreen;
    private static final float SLOT_WIDTH = 250f;
    private static final float SLOT_HEIGHT = 150f;
    private static final float NAV_BTN_SIZE = 80f;

    public SavedGamesScreen(AngryBirdsGame game, boolean saveMode, Screen lastScreen) {
        this.game = game;
        this.saveMode = saveMode;
        this.lastScreen = lastScreen;
        setupScreen();
    }

    private void setupScreen() {
        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        createBackground();
        setupUI();
    }

    private void createBackground() {
        Texture bgImg = AssetManager.getInstance().getTexture("levelscreen_bg.jpg");
        Image bg = new Image(new TextureRegion(bgImg));
        bg.setFillParent(true);
        uiStage.addActor(bg);
    }

    private void setupUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        addNavigationButton();
        addTitle(mainTable);
        addSaveSlots(mainTable);

        uiStage.addActor(mainTable);
    }

    private void addNavigationButton() {
        Texture backImg = AssetManager.getInstance().getTexture("backbutton.png");
        ImageButton backBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(backImg)));
        backBtn.setSize(NAV_BTN_SIZE, NAV_BTN_SIZE);
        backBtn.setPosition(30, Gdx.graphics.getHeight() - NAV_BTN_SIZE - 30);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleBackButton();
            }
        });
        uiStage.addActor(backBtn);
    }

    private void handleBackButton() {
        if (lastScreen instanceof MainMenuScreen) {
            game.setScreen(new MainMenuScreen(game));
        } else if (lastScreen instanceof GameScreen) {
            GameScreen gameScreen = (GameScreen) lastScreen;
            game.setScreen(gameScreen, false);
            if (!gameScreen.isPaused()) {
                gameScreen.togglePause();
            }
            Gdx.input.setInputProcessor(gameScreen.getPauseMenu().getStage());
        }
    }

    private void addTitle(Table mainTable) {
        Label.LabelStyle titleStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        titleStyle.font = uiSkin.getFont("default-font");
        titleStyle.fontColor = Color.WHITE;
        Label titleLabel = new Label(saveMode ? "Save Game" : "Load Game", titleStyle);
        titleLabel.setFontScale(2f);

        Table headerTable = new Table();
        headerTable.add(titleLabel).expandX();
        mainTable.add(headerTable).padBottom(50).row();
    }

    private void addSaveSlots(Table mainTable) {
        Table slotsTable = new Table();
        for (int i = 1; i <= 3; i++) {
            Stack slotStack = createSlot(i);
            slotsTable.add(slotStack).size(SLOT_WIDTH, SLOT_HEIGHT).pad(20);
        }
        mainTable.add(slotsTable);
    }

    private Stack createSlot(int slotNumber) {
        Stack slotStack = new Stack();

        Texture slotImg = AssetManager.getInstance().getTexture("button_bg.png");
        Image slotBg = new Image(new TextureRegionDrawable(new TextureRegion(slotImg)));
        slotStack.add(slotBg);

        Table content = new Table();
        Label.LabelStyle labelStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        labelStyle.font = uiSkin.getFont("default-font");
        labelStyle.fontColor = Color.WHITE;

        Label slotLabel = new Label("Slot " + slotNumber, labelStyle);
        slotLabel.setFontScale(1.5f);
        content.add(slotLabel).pad(10).row();

        Label infoLabel = new Label("Empty Slot", labelStyle);
        content.add(infoLabel).pad(10);

        slotStack.add(content);

        final int slot = slotNumber;
        slotStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleSlotClick(slot);
            }
        });

        return slotStack;
    }

    private void handleSlotClick(int slot) {
        if (saveMode) {
            showSaveSuccess();
            returnToGame();
        } else {
            loadGame();
        }
    }

    private void showSaveSuccess() {
        Table popup = new Table();
        popup.setFillParent(true);
        popup.bottom();

        Label.LabelStyle style = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        style.font = uiSkin.getFont("default-font");
        style.fontColor = Color.WHITE;

        Label msg = new Label("Game Saved Successfully!", style);
        msg.setFontScale(1.5f);

        Table bgTable = new Table();
        bgTable.setBackground(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("button_bg.png"))));
        bgTable.add(msg).pad(15);

        popup.add(bgTable).padBottom(50);
        uiStage.addActor(popup);

        popup.getColor().a = 1f;
        popup.addAction(sequence(delay(2f), fadeOut(1f), removeActor()));
    }

    private void returnToGame() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (lastScreen instanceof GameScreen) {
                    GameScreen gameScreen = (GameScreen) lastScreen;
                    game.setScreen(gameScreen, false);
                    if (!gameScreen.isPaused()) {
                        gameScreen.togglePause();
                    }
                    Gdx.input.setInputProcessor(gameScreen.getPauseMenu().getStage());
                }
            }
        }, 3);
    }

    private void loadGame() {
        LevelSingle level = LevelsScreen.gameLevels.get(0);
        game.setScreen(new GameScreen(game, level));
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
