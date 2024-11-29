package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PauseMenuScreen {
    private final AngryBirdsGame game;
    private final GameScreen gameScreen;
    private Stage uiStage;
    private Skin uiSkin;
    private final ShapeRenderer overlay;

    private Table pauseMenu;
    private Table winLossMenu;
    private static final float WIN_MENU_WIDTH = 800f;
    private static final float WIN_MENU_HEIGHT = 500f;
    private static final float PAUSE_MENU_WIDTH = 700f;
    private static final float PAUSE_MENU_HEIGHT = 750f;
    private static final float CLOSE_BTN_SIZE = 50f;
    private static final float BTN_WIDTH = 250f;
    private static final float BTN_HEIGHT = 60f;
    private static final float STAR_SIZE = 100f;
    private static final float BTN_SIZE = 80f;

    public PauseMenuScreen(AngryBirdsGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.overlay = new ShapeRenderer();
        this.uiStage = new Stage(new ScreenViewport());
        this.uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        setupPauseMenu();
    }

    private void setupPauseMenu() {
        uiStage = new Stage(new ScreenViewport());
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
    }

    public void togglePauseMenu() {
        clearMenus();
        createAndShowPauseMenu();
        render(Gdx.graphics.getDeltaTime());
    }


    private void clearMenus() {
        if (pauseMenu != null) pauseMenu.remove();
        if (winLossMenu != null) winLossMenu.remove();
    }

    private void createAndShowPauseMenu() {
        pauseMenu = new Table();
        pauseMenu.setBackground(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("button_bg.png"))));

        Label.LabelStyle titleStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        titleStyle.font = uiSkin.getFont("default-font");
        titleStyle.fontColor = Color.WHITE;
        Label titleLabel = new Label("PAUSED", titleStyle);
        titleLabel.setFontScale(2.5f);

        ImageButton closeBtn = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(AssetManager.getInstance().getTexture("close.png"))));
        closeBtn.setSize(CLOSE_BTN_SIZE, CLOSE_BTN_SIZE);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.togglePause();
            }
        });

        Table headerTable = new Table();
        headerTable.add(titleLabel).expandX().padLeft(CLOSE_BTN_SIZE * 1.4f).padTop(20);
        headerTable.add(closeBtn).size(CLOSE_BTN_SIZE).padTop(20);
        pauseMenu.add(headerTable).width(PAUSE_MENU_WIDTH).row();

        addMenuButton(pauseMenu, "Resume", () -> gameScreen.togglePause());
        addMenuButton(pauseMenu, "Restart", () -> gameScreen.restartLevel());
        addMenuButton(pauseMenu, "Save Game", () -> game.setScreen(new SavedGamesScreen(game, true, gameScreen), false));
        addMenuButton(pauseMenu, "Audio Settings", () -> game.setScreen(new SettingsScreen(game, gameScreen), false));
        addMenuButton(pauseMenu, "Quit to Menu", () -> gameScreen.quitToMainMenu());

        pauseMenu.setSize(PAUSE_MENU_WIDTH, PAUSE_MENU_HEIGHT);
        pauseMenu.setPosition(
            (Gdx.graphics.getWidth() - PAUSE_MENU_WIDTH) / 2,
            (Gdx.graphics.getHeight() - PAUSE_MENU_HEIGHT) / 2
        );

        uiStage.addActor(pauseMenu);
    }

    void showWinLoss(boolean isWin, int stars) {
        clearMenus();

        Table contentTable = new Table();
        contentTable.setBackground(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("button_bg.png"))));
        contentTable.center();

        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        titleStyle.font = uiSkin.getFont("default-font");
        titleStyle.fontColor = Color.WHITE;
        Label msg = new Label(isWin ? "LEVEL COMPLETE!" : "LEVEL FAILED!", titleStyle);
        msg.setFontScale(2.5f);
        contentTable.add(msg).padTop(30).row();

        // Stars for win condition
        if (isWin) {
            Table starsTable = new Table();
            Texture starTexture = AssetManager.getInstance().getTexture("star_" + stars + ".png");
            Image star = new Image(new TextureRegion(starTexture));
            starsTable.add(star).size(STAR_SIZE).pad(10);
            contentTable.add(starsTable).padTop(20).row();

            if (stars < 2) {
                Label unlockMsg = new Label("Get 2 or more stars to unlock next level!", titleStyle);
                unlockMsg.setFontScale(1.2f);
                unlockMsg.setColor(Color.YELLOW);
                contentTable.add(unlockMsg).padTop(10).row();
            }
        }

        // Buttons
        Table buttonsTable = new Table();

        // Retry button
        ImageButton retryBtn = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(AssetManager.getInstance().getTexture("retry.png"))));
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.restartLevel();
            }
        });
        buttonsTable.add(retryBtn).size(BTN_SIZE).pad(20);

        // Next level button (only for wins with 2 or more stars)
        if (isWin && stars >= 2) {
            ImageButton nextBtn = new ImageButton(new TextureRegionDrawable(
                new TextureRegion(AssetManager.getInstance().getTexture("next.png"))));
            nextBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Get current level and create next level
                    int currentLevelNum = gameScreen.getCurrentLevel().getNumber();
                    int nextLevelNum = currentLevelNum + 1;

                    // Check if next level exists in the list
                    if (nextLevelNum <= LevelsScreen.gameLevels.size()) {
                        LevelSingle nextLevel = LevelsScreen.gameLevels.get(nextLevelNum - 1);
                        if (nextLevel != null) {
                            game.setScreen(new GameScreen(game, nextLevel));
                        }
                    } else {
                        // If no next level, return to level selection
                        game.setScreen(new LevelsScreen(game));
                    }
                }
            });
            buttonsTable.add(nextBtn).size(BTN_SIZE).pad(20);
        }

        // Menu button
        ImageButton menuBtn = new ImageButton(new TextureRegionDrawable(
            new TextureRegion(AssetManager.getInstance().getTexture("lines.png"))));
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.quitToMainMenu();
            }
        });
        buttonsTable.add(menuBtn).size(BTN_SIZE).pad(20);

        contentTable.add(buttonsTable).padTop(30).padBottom(30).row();

        contentTable.setSize(WIN_MENU_WIDTH, WIN_MENU_HEIGHT);
        contentTable.setPosition(
            (Gdx.graphics.getWidth() - WIN_MENU_WIDTH) / 2,
            (Gdx.graphics.getHeight() - WIN_MENU_HEIGHT) / 2
        );

        uiStage.addActor(contentTable);
        drawOverlay();
        Gdx.input.setInputProcessor(uiStage);
    }

    private void addMenuButton(Table menu, String text, Runnable action) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(uiSkin.get(TextButton.TextButtonStyle.class));
        Texture btnTexture = AssetManager.getInstance().getTexture("pause_button_bg.png");
        style.up = new TextureRegionDrawable(new TextureRegion(btnTexture));
        style.down = style.up;
        style.over = style.up;
        style.font = uiSkin.getFont("default-font");
        style.fontColor = Color.WHITE;

        TextButton button = new TextButton(text, style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        menu.add(button).size(BTN_WIDTH, BTN_HEIGHT).padBottom(15).row();
    }

    private ImageButton createImageButton(String textureName, Runnable action) {
        Texture btnTexture = AssetManager.getInstance().getTexture(textureName);
        ImageButton button = new ImageButton(new TextureRegionDrawable(new TextureRegion(btnTexture)));
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return button;
    }

    private void drawOverlay() {
        if (game.gameBatch.isDrawing()) {
            game.gameBatch.end();
        }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        overlay.begin(ShapeRenderer.ShapeType.Filled);
        overlay.setColor(0, 0, 0, 0.7f);
        overlay.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        overlay.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void render(float delta) {
        drawOverlay();
        if (uiStage != null) {
            uiStage.act(Math.min(delta, 1/30f));
            uiStage.draw();
        }
    }

    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    public Stage getStage() {
        return uiStage;
    }

    public void dispose() {
        uiStage.dispose();
        uiSkin.dispose();
        overlay.dispose();
    }
}
