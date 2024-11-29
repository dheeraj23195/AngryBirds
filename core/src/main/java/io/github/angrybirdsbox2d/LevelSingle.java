package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

public class LevelSingle {
    private final int levelNum;
    private int stars;
    private boolean unlocked;
    private final List<Block> blocksList;
    private final List<Pig> pigsList;
    private final List<Bird> birdsList;
    private static final long serialVersionUID = 1L;


    public LevelSingle(int levelNum, int stars, boolean unlocked) {
        this.levelNum = levelNum;
        this.stars = stars;
        this.unlocked = unlocked;
        this.blocksList = new ArrayList<>();
        this.pigsList = new ArrayList<>();
        this.birdsList = new ArrayList<>();
        buildLevel();
    }

    public int getNumber() {
        return levelNum;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean state) {
        unlocked = state;
    }

    public int getRating() {
        return stars;
    }

    public void setRating(int newRating) {
        stars = newRating;
    }

    public void addBlock(Block block) {
        blocksList.add(block);
    }

    public void addPig(Pig pig) {
        pigsList.add(pig);
    }

    public void addBird(Bird bird) {
        birdsList.add(bird);
    }

    public List<Block> getBlocks() {
        return blocksList;
    }

    public List<Pig> getPigs() {
        return pigsList;
    }

    public List<Bird> getBirds() {
        return birdsList;
    }

    private void buildLevel() {
        float baseY = 100f;
        float blockSize = 50f;
        float edgeSize = 10f;
        float pigSize = 35f;
        float pigYExtra = 10f;
        float pigXPos = (blockSize - edgeSize * 2 - pigSize) / 2;
        float pigYPos = edgeSize + pigYExtra;

        switch (levelNum) {
            case 1:
                buildLevel1(baseY, blockSize, edgeSize, pigSize, pigXPos, pigYPos);
                break;
            case 2:
                buildLevel2(baseY, blockSize, edgeSize, pigSize, pigXPos, pigYPos);
                break;
            case 3:
                buildLevel3(baseY, blockSize, edgeSize, pigSize, pigXPos, pigYPos);
                break;
            default:
                buildDefaultLevel(baseY, blockSize, edgeSize, pigSize, pigXPos, pigYPos);
                break;
        }

        setupBirdsForLevel();
    }

    private void buildLevel1(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.65f;

        // Pyramid structure
        addBlock(new SteelBlock(startX, baseY));
        addBlock(new SteelBlock(startX + blockSize * 1.2f, baseY));
        addBlock(new SteelBlock(startX + blockSize * 2.4f, baseY));

        // Second layer
        addBlock(new GlassBlock(startX + blockSize * 0.6f, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 1.8f, baseY + blockSize));

        // Top
        addBlock(new WoodBlock(startX + blockSize * 1.2f, baseY + blockSize * 2));

        // Pig at the top
        Pig pig = new Pig(50, PigType.SMALL);
        pig.setX(startX + blockSize * 1.2f + pigSize/2);
        pig.setY(baseY + blockSize * 2 + pigSize/2);
        addPig(pig);
    }

    private void buildLevel2(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.65f;

        // Two towers with pigs
        // Left tower
        addBlock(new WoodBlock(startX, baseY));
        addBlock(new WoodBlock(startX, baseY + blockSize));
        addBlock(new GlassBlock(startX, baseY + blockSize * 2));

        // Right tower
        addBlock(new WoodBlock(startX + blockSize * 3, baseY));
        addBlock(new WoodBlock(startX + blockSize * 3, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 3, baseY + blockSize * 2));

        // Bridge between towers
        addBlock(new SteelBlock(startX + blockSize * 1.5f, baseY + blockSize * 1.5f));

        // Pigs
        Pig pig1 = new Pig(50, PigType.SMALL);
        pig1.setX(startX + pigSize/2);
        pig1.setY(baseY + blockSize * 2 + pigSize);
        addPig(pig1);

        Pig pig2 = new Pig(50, PigType.MEDIUM);
        pig2.setX(startX + blockSize * 3 + pigSize/2);
        pig2.setY(baseY + blockSize * 2 + pigSize);
        addPig(pig2);
    }

    private void buildLevel3(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.65f;

        // Base platform
        for(int i = 0; i < 5; i++) {
            addBlock(new SteelBlock(startX + blockSize * i * 1.2f, baseY));
        }

        // Middle layer - alternating gaps
        addBlock(new GlassBlock(startX, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 2.4f, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 4.8f, baseY + blockSize));

        // Top layer
        addBlock(new WoodBlock(startX + blockSize * 1.2f, baseY + blockSize * 2));
        addBlock(new WoodBlock(startX + blockSize * 3.6f, baseY + blockSize * 2));

        // Pigs in the gaps
        Pig pig1 = new Pig(50, PigType.SMALL);
        pig1.setX(startX + blockSize * 1.2f + pigSize/2);
        pig1.setY(baseY + blockSize + pigSize/2);
        addPig(pig1);

        Pig pig2 = new Pig(50, PigType.MEDIUM);
        pig2.setX(startX + blockSize * 3.6f + pigSize/2);
        pig2.setY(baseY + blockSize + pigSize/2);
        addPig(pig2);

        Pig pig3 = new Pig(50, PigType.LARGE);
        pig3.setX(startX + blockSize * 2.4f + pigSize/2);
        pig3.setY(baseY + blockSize * 2 + pigSize/2);
        addPig(pig3);
    }

    private void buildDefaultLevel(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.65f;

        // Castle-like structure
        // Base
        addBlock(new SteelBlock(startX, baseY));
        addBlock(new SteelBlock(startX + blockSize * 1.2f, baseY));
        addBlock(new SteelBlock(startX + blockSize * 2.4f, baseY));
        addBlock(new SteelBlock(startX + blockSize * 3.6f, baseY));

        // Second layer towers
        addBlock(new GlassBlock(startX, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 3.6f, baseY + blockSize));

        // Middle section
        addBlock(new WoodBlock(startX + blockSize * 1.2f, baseY + blockSize));
        addBlock(new WoodBlock(startX + blockSize * 2.4f, baseY + blockSize));

        // Top towers
        addBlock(new GlassBlock(startX, baseY + blockSize * 2));
        addBlock(new GlassBlock(startX + blockSize * 3.6f, baseY + blockSize * 2));

        // Pigs
        Pig pig1 = new Pig(50, PigType.SMALL);
        pig1.setX(startX + blockSize * 1.2f + pigSize/2);
        pig1.setY(baseY + blockSize + pigSize/2);
        addPig(pig1);

        Pig pig2 = new Pig(50, PigType.MEDIUM);
        pig2.setX(startX + blockSize * 2.4f + pigSize/2);
        pig2.setY(baseY + blockSize + pigSize/2);
        addPig(pig2);
    }
/*
    private void buildDefaultLevel(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.5f;  // Changed from 0.7f for better spacing

        // Place individual blocks with good spacing for testing
        addBlock(new WoodBlock(startX, baseY));
        addBlock(new GlassBlock(startX + blockSize * 2, baseY));
        addBlock(new SteelBlock(startX + blockSize * 4, baseY));

        // Place pigs with spacing
        Pig pig1 = new Pig(50, PigType.SMALL);
        pig1.setX(startX + blockSize * 6);
        pig1.setY(baseY + pigYPos);
        addPig(pig1);

        Pig pig2 = new Pig(50, PigType.MEDIUM);
        pig2.setX(startX + blockSize * 8);
        pig2.setY(baseY + pigYPos);
        addPig(pig2);

        Pig pig3 = new Pig(50, PigType.LARGE);
        pig3.setX(startX + blockSize * 10);
        pig3.setY(baseY + pigYPos);
        addPig(pig3);
    }*/

    private void setupBirdsForLevel() {
        List<Bird> availableBirds = LevelsScreen.getAvailableBirdsForLevel(this.levelNum);
        for (Bird bird : availableBirds) {
            birdsList.add(new Bird(bird.getHp(), bird.getBirdType(), bird.getUnlockLevel()));
        }
    }
}
