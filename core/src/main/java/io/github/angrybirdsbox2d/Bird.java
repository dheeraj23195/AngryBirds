package io.github.angrybirdsbox2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Bird implements GameObject, Serializable {
    private float x, y;
    private int health;
    private BirdType type;
    private int levelToUnlock;
    private Vector2 position;
    private Vector2 dragVector;
    private boolean isDragged;
    private final float radius;
    private final int damage;
    private static final long serialVersionUID = 5L;


    public Bird(int health, BirdType type, int levelToUnlock) {
        this.health = health;
        this.type = type;
        this.levelToUnlock = levelToUnlock;
        this.position = new Vector2();
        this.dragVector = new Vector2();
        this.radius = 25f;
        this.isDragged = false;

        switch(type) {
            case RED:
                this.damage = 800;    // Increased from 350
                break;
            case YELLOW:
                this.damage = 600;    // Increased from 250
                break;
            case BLACK:
                this.damage = 700;    // Increased from 300
                break;
            default:
                this.damage = 700;
        }
    }

    @Override
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) this.health = 0;
    }

    public float getX() {
        return x;
    }


    public float getY() {
        return y;
    }


    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 pos) {
        position = pos;
        x = pos.x;
        y = pos.y;
    }

    public Vector2 getDragVector() {
        return dragVector;
    }

    public void setDragVector(Vector2 vec) {
        dragVector = vec;
    }

    public boolean isDragged() {
        return isDragged;
    }

    public void setDragged(boolean dragged) {
        isDragged = dragged;
    }

    public float getRadius() {
        return radius;
    }

    public int getHp() {
        return health;
    }

    public void setHp(int hp) {
        this.health = hp;
    }

    public BirdType getBirdType() {
        return type;
    }

    public void setBirdType(BirdType bType) {
        this.type = bType;
    }

    public int getUnlockLevel() {
        return levelToUnlock;
    }
    @Override
    public void setX(float x) {
        this.x = x;
        position.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
        position.y = y;
    }

    public void setUnlockLevel(int level) {
        this.levelToUnlock = level;
    }

    public int getDamage() {
        return damage;
    }

    public Texture getTexture() {
        String birdImg = type.name().toLowerCase() + "_bird.png";
        return AssetManager.getInstance().getTexture(birdImg);
    }
}
