package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PauseMenuScreen {
    private final AngryBirdsGame game;
    private final GameScreen gameScreen;
    private Stage uiStage;
    private Skin uiSkin;
    private final ShapeRenderer overlay;

    private static final float MENU_WIDTH = 700f;
    private static final float MENU_HEIGHT = 750f;
    private static final float BTN_WIDTH = 250f;
    private static final float BTN_HEIGHT = 60f;
    private static final float CLOSE_BTN_SIZE = 50f;
    private static final float SPACING = 20f;
    private static final float WIN_MENU_WIDTH = 800f;
    private static final float WIN_MENU_HEIGHT = 500f;
    private static final float STAR_SIZE = 100f;
    private static final float WIN_BTN_SIZE = 80f;


    public PauseMenuScreen(AngryBirdsGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.overlay = new ShapeRenderer();
        setupPauseMenu();
    }

    private void setupPauseMenu() {
        uiStage = new Stage(new ScreenViewport());
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        Table menuTable = createMenuTable();
        addMenuHeader(menuTable);
        addMenuButtons(menuTable);

        menuTable.setSize(MENU_WIDTH, MENU_HEIGHT);
        menuTable.setPosition(
            (Gdx.graphics.getWidth() - MENU_WIDTH) / 2,
            (Gdx.graphics.getHeight() - MENU_HEIGHT) / 2
        );

        uiStage.addActor(menuTable);
    }

    private Table createMenuTable() {
        Table menuTable = new Table();
        menuTable.setBackground(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("button_bg.png"))));
        menuTable.center();
        return menuTable;
    }

    private void addMenuHeader(Table menuTable) {
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
        headerTable.add(titleLabel).expandX().padLeft(SPACING * 3.5f).padTop(SPACING);
        headerTable.add(closeBtn).size(CLOSE_BTN_SIZE).padTop(SPACING);
        menuTable.add(headerTable).width(MENU_WIDTH).padBottom(20f).row();
    }

    private void addMenuButtons(Table menuTable) {
        TextButton.TextButtonStyle btnStyle = createButtonStyle();

        TextButton resumeBtn = createButton("Resume", btnStyle, () -> gameScreen.togglePause());
        TextButton restartBtn = createButton("Restart", btnStyle, () -> gameScreen.restartLevel());
        TextButton saveBtn = createButton("Save Game", btnStyle, () -> game.setScreen(new SavedGamesScreen(game, true, gameScreen), false));
        TextButton audioBtn = createButton("Audio Settings", btnStyle, () -> game.setScreen(new SettingsScreen(game, gameScreen), false));
        TextButton winBtn = createButton("Win", btnStyle, () -> showWinLoss(true));
        TextButton loseBtn = createButton("Lose", btnStyle, () -> showWinLoss(false));
        TextButton quitBtn = createButton("Quit to Menu", btnStyle, () -> gameScreen.quitToMainMenu());

        TextButton[] buttons = {
            resumeBtn, restartBtn, saveBtn, audioBtn, winBtn, loseBtn, quitBtn
        };

        for (TextButton btn : buttons) {
            menuTable.add(btn).size(BTN_WIDTH, BTN_HEIGHT).padBottom(15f).row();
        }
    }

    private void showWinLoss(boolean isWin) {
        Table popup = new Table();
        popup.setFillParent(true);
        popup.center();
        Table contentTable = new Table();
        contentTable.setBackground(new TextureRegionDrawable(new TextureRegion(
            AssetManager.getInstance().getTexture("button_bg.png"))));
        contentTable.center();
        Label.LabelStyle titleStyle = new Label.LabelStyle(uiSkin.get(Label.LabelStyle.class));
        titleStyle.font = uiSkin.getFont("default-font");
        titleStyle.fontColor = Color.WHITE;
        Label msg = new Label(isWin ? "YOU WIN!" : "YOU LOSE!", titleStyle);
        msg.setFontScale(2.5f);
        contentTable.add(msg).padTop(30).row();

        if (isWin) {
            Table starsTable = new Table();
            Texture starTexture = AssetManager.getInstance().getTexture("star_3.png");
            Image star = new Image(new TextureRegion(starTexture));
            starsTable.add(star).size(STAR_SIZE).pad(10);
            contentTable.add(starsTable).padTop(20).row();
            Table buttonsTable = new Table();
            ImageButton nextBtn = createImageButton("next.png", () -> {
                gameScreen.quitToMainMenu();
            });
            ImageButton retryBtn = createImageButton("retry.png", () -> {
                gameScreen.restartLevel();
                gameScreen.togglePause();
            });
            ImageButton menuBtn = createImageButton("lines.png", () -> {
                gameScreen.quitToMainMenu();
            });

            buttonsTable.add(retryBtn).size(WIN_BTN_SIZE).pad(20);
            buttonsTable.add(nextBtn).size(WIN_BTN_SIZE).pad(20);
            buttonsTable.add(menuBtn).size(WIN_BTN_SIZE).pad(20);

            contentTable.add(buttonsTable).padTop(30).padBottom(30).row();
        } else {
            Table buttonsTable = new Table();

            ImageButton retryBtn = createImageButton("retry.png", () -> {
                gameScreen.restartLevel();
                gameScreen.togglePause();
            });

            ImageButton menuBtn = createImageButton("lines.png", () -> {
                gameScreen.quitToMainMenu();
            });

            buttonsTable.add(retryBtn).size(WIN_BTN_SIZE).pad(20);
            buttonsTable.add(menuBtn).size(WIN_BTN_SIZE).pad(20);

            contentTable.add(buttonsTable).padTop(30).padBottom(30).row();
        }

        contentTable.setSize(WIN_MENU_WIDTH, WIN_MENU_HEIGHT);
        contentTable.setPosition(
            (Gdx.graphics.getWidth() - WIN_MENU_WIDTH) / 2,
            (Gdx.graphics.getHeight() - WIN_MENU_HEIGHT) / 2
        );

        uiStage.addActor(contentTable);
        drawOverlay();
    }

    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(uiSkin.get(TextButton.TextButtonStyle.class));
        Texture btnTexture = AssetManager.getInstance().getTexture("pause_button_bg.png");
        style.up = new TextureRegionDrawable(new TextureRegion(btnTexture));
        style.down = style.up;
        style.over = style.up;
        style.font = uiSkin.getFont("default-font");
        style.fontColor = Color.WHITE;
        return style;
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

    private TextButton createButton(String text, TextButton.TextButtonStyle style, Runnable action) {
        TextButton button = new TextButton(text, style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }

    public void render(float delta) {
        drawOverlay();
        uiStage.act(delta);
        uiStage.draw();
    }

    private void drawOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        overlay.begin(ShapeRenderer.ShapeType.Filled);
        overlay.setColor(0, 0, 0, 0.7f);
        overlay.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        overlay.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
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
