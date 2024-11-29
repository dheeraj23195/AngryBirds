package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LevelSingle implements Serializable {
    private static final long serialVersionUID = 101L;
    private final int levelNum;
    private int stars;
    private boolean unlocked;
    private ArrayList<SerializableGameObject> serializableObjects;
    private transient List<Block> blocksList;
    private transient List<Pig> pigsList;
    private transient List<Bird> birdsList;

    private static class SerializableGameObject implements Serializable {
        private static final long serialVersionUID = 102L;
        String type;
        float x;
        float y;
        int hp;
        PigType pigType;
        BirdType birdType;
        int unlockLevel;
    }

    public LevelSingle(int levelNum, int stars, boolean unlocked) {
        this.levelNum = levelNum;
        this.stars = stars;
        this.unlocked = unlocked;
        this.blocksList = new ArrayList<>();
        this.pigsList = new ArrayList<>();
        this.birdsList = new ArrayList<>();
        buildLevel();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        serializableObjects = new ArrayList<>();

        // Save blocks
        for (Block block : blocksList) {
            SerializableGameObject obj = new SerializableGameObject();
            if (block instanceof WoodBlock) obj.type = "WOOD_BLOCK";
            else if (block instanceof GlassBlock) obj.type = "GLASS_BLOCK";
            else if (block instanceof SteelBlock) obj.type = "STEEL_BLOCK";
            obj.x = block.getX();
            obj.y = block.getY();
            serializableObjects.add(obj);
        }

        // Save pigs
        for (Pig pig : pigsList) {
            SerializableGameObject obj = new SerializableGameObject();
            obj.type = "PIG";
            obj.x = pig.getX();
            obj.y = pig.getY();
            obj.hp = pig.getHp();
            obj.pigType = pig.getPigType();
            serializableObjects.add(obj);
        }

        // Save birds
        for (Bird bird : birdsList) {
            SerializableGameObject obj = new SerializableGameObject();
            obj.type = "BIRD";
            obj.hp = bird.getHp();
            obj.birdType = bird.getBirdType();
            obj.unlockLevel = bird.getUnlockLevel();
            serializableObjects.add(obj);
        }

        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        blocksList = new ArrayList<>();
        pigsList = new ArrayList<>();
        birdsList = new ArrayList<>();

        if (serializableObjects != null) {
            for (SerializableGameObject obj : serializableObjects) {
                switch (obj.type) {
                    case "WOOD_BLOCK":
                        Block woodBlock = new WoodBlock(obj.x, obj.y);
                        blocksList.add(woodBlock);
                        break;
                    case "GLASS_BLOCK":
                        Block glassBlock = new GlassBlock(obj.x, obj.y);
                        blocksList.add(glassBlock);
                        break;
                    case "STEEL_BLOCK":
                        Block steelBlock = new SteelBlock(obj.x, obj.y);
                        blocksList.add(steelBlock);
                        break;
                    case "PIG":
                        Pig pig = new Pig(obj.hp, obj.pigType);
                        pig.setX(obj.x);
                        pig.setY(obj.y);
                        pigsList.add(pig);
                        break;
                    case "BIRD":
                        Bird bird = new Bird(obj.hp, obj.birdType, obj.unlockLevel);
                        birdsList.add(bird);
                        break;
                }
            }
        } else {
            buildLevel(); // Fallback if no saved data
        }
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
