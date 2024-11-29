package io.github.angrybirdsbox2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Pig implements GameObject, Serializable {
    private float x, y;
    private int hp;
    private PigType pigType;
    private Vector2 velocity;
    private boolean isFalling;
    private static final float GRAVITY = -9.81f/6;
    private static final float BOUNCE_DAMPING = 0.5f;
    private static final float MIN_VELOCITY = 50f;
    private static final long serialVersionUID = 6L;
    private float rotation = 0f;
    private final int maxHealth;

    public Pig(int hp, PigType pigType) {
        this.hp = hp;
        this.pigType = pigType;
        this.maxHealth = hp;
        this.velocity = new Vector2(0, 0);
        this.isFalling = false;
    }

    @Override
    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0) this.hp = 0;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public PigType getPigType() {
        return pigType;
    }

    public void setPigType(PigType pigType) {
        this.pigType = pigType;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public boolean isFalling() {
        return isFalling;
    }

    public void setFalling(boolean falling) {
        this.isFalling = falling;
    }

    public static float getGravity() {
        return GRAVITY;
    }

    public static float getBounceDamping() {
        return BOUNCE_DAMPING;
    }

    public static float getMinVelocity() {
        return MIN_VELOCITY;
    }

    public Texture getTexture() {
        String textureName = pigType.name().toLowerCase() + "_pig.png";
        return AssetManager.getInstance().getTexture(textureName);
    }
}
