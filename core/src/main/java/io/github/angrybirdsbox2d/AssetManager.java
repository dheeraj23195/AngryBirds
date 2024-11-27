package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static AssetManager manager;
    private final Map<String, Texture> gameTextures;

    private AssetManager() {
        gameTextures = new HashMap<>();
        loadTextures();
    }

    public static AssetManager getInstance() {
        if (manager == null) {
            manager = new AssetManager();
        }
        return manager;
    }

    void loadTextures() {
        gameTextures.put("main_screen_bg.jpg", new Texture("main_screen_bg.jpg"));
        gameTextures.put("levelscreen_bg.jpg", new Texture("levelscreen_bg.jpg"));
        gameTextures.put("backgroundlevel.png", new Texture("backgroundlevel.png"));
        gameTextures.put("pause_button_bg.png", new Texture("pause_button_bg.png"));
        gameTextures.put("button_bg.png", new Texture("button_bg.png"));
        gameTextures.put("level_button.png", new Texture("level_button.png"));
        gameTextures.put("locked_button.png", new Texture("locked_button.png"));
        gameTextures.put("backbutton.png", new Texture("backbutton.png"));
        gameTextures.put("close.png", new Texture("close.png"));
        gameTextures.put("pause.png", new Texture("pause.png"));
        gameTextures.put("gear_icon.png", new Texture("gear_icon.png"));
        gameTextures.put("star_3.png", new Texture("star_3.png"));
        gameTextures.put("star_2.png", new Texture("star_2.png"));
        gameTextures.put("star_1.png", new Texture("star_1.png"));
        gameTextures.put("lines.png", new Texture("lines.png"));
        gameTextures.put("retry.png", new Texture("retry.png"));
        gameTextures.put("next.png", new Texture("next.png"));
        gameTextures.put("level_0star.png", new Texture("level_0star.png"));
        gameTextures.put("level_1star.png", new Texture("level_1star.png"));
        gameTextures.put("level_2star.png", new Texture("level_2star.png"));
        gameTextures.put("level_3star.png", new Texture("level_3star.png"));
        gameTextures.put("catapult_front.png", new Texture("catapult_front.png"));
        gameTextures.put("catapult_back.png", new Texture("catapult_back.png"));
        gameTextures.put("red_bird.png", new Texture("red_bird.png"));
        gameTextures.put("yellow_bird.png", new Texture("yellow_bird.png"));
        gameTextures.put("black_bird.png", new Texture("black_bird.png"));
        gameTextures.put("small_pig.png", new Texture("small_pig.png"));
        gameTextures.put("medium_pig.png", new Texture("medium_pig.png"));
        gameTextures.put("large_pig.png", new Texture("large_pig.png"));
        gameTextures.put("wood_block.png", new Texture("wood_block.png"));
        gameTextures.put("glass_block.png", new Texture("glass_block.png"));
        gameTextures.put("steel_block.png", new Texture("steel_block.png"));
    }

    public Texture getTexture(String imgName) {
        return gameTextures.get(imgName);
    }
}
