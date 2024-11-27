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
        float blockSize = 100f;
        float edgeSize = 10f;
        float pigSize = 50f;
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
        float startX = Gdx.graphics.getWidth() * 0.7f;
        addBlock(new WoodBlock(startX, baseY));
        addBlock(new GlassBlock(startX + blockSize, baseY));
        addBlock(new SteelBlock(startX + blockSize * 2, baseY));
        addBlock(new GlassBlock(startX + blockSize / 2, baseY + blockSize));
        addBlock(new WoodBlock(startX + blockSize * 1.5f, baseY + blockSize));

        Pig pig = new Pig(50, PigType.SMALL);
        pig.setX(startX + blockSize + edgeSize + pigXPos);
        pig.setY(baseY + pigYPos);
        addPig(pig);
    }

    private void buildLevel2(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.7f;
        addBlock(new SteelBlock(startX, baseY));
        addBlock(new WoodBlock(startX, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 2, baseY));
        addBlock(new SteelBlock(startX + blockSize * 2, baseY + blockSize));
        addBlock(new WoodBlock(startX + blockSize, baseY + blockSize));

        Pig pig1 = new Pig(50, PigType.SMALL);
        pig1.setX(startX + edgeSize + pigXPos);
        pig1.setY(baseY + pigYPos);
        addPig(pig1);

        Pig pig2 = new Pig(50, PigType.SMALL);
        pig2.setX(startX + blockSize * 2 + edgeSize + pigXPos);
        pig2.setY(baseY + pigYPos);
        addPig(pig2);
    }

    private void buildLevel3(float baseY, float blockSize, float edgeSize, float pigSize, float pigXPos, float pigYPos) {
        float startX = Gdx.graphics.getWidth() * 0.7f;

        for(int i = 0; i < 4; i++) {
            addBlock(new SteelBlock(startX + blockSize * i, baseY));
        }
        addBlock(new GlassBlock(startX + blockSize, baseY + blockSize));
        addBlock(new GlassBlock(startX + blockSize * 2, baseY + blockSize));
        addBlock(new WoodBlock(startX + blockSize * 1.5f, baseY + blockSize * 2));

        Pig pig1 = new Pig(50, PigType.SMALL);
        pig1.setX(startX + blockSize + edgeSize + pigXPos);
        pig1.setY(baseY + blockSize + pigYPos);
        addPig(pig1);

        Pig pig2 = new Pig(50, PigType.SMALL);
        pig2.setX(startX + blockSize * 2 + edgeSize + pigXPos);
        pig2.setY(baseY + blockSize + pigYPos);
        addPig(pig2);
    }

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
    }

    private void setupBirdsForLevel() {
        List<Bird> availableBirds = LevelsScreen.getAvailableBirdsForLevel(this.levelNum);
        for (Bird bird : availableBirds) {
            birdsList.add(new Bird(bird.getHp(), bird.getBirdType(), bird.getUnlockLevel()));
        }
    }
}
