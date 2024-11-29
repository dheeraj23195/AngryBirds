package io.github.angrybirdsbox2d;

import java.io.Serializable;

public abstract class Block implements GameObject, Destructible, Serializable {
    public static final float WIDTH = 50;
    public static final float HEIGHT = 50;
    private static final long serialVersionUID = 1L;

    protected float x, y;
    protected float health;
    protected float maxHealth;
    protected float destructionProgress;

    public Block(float x, float y, float initialHealth) {
        this.x = x;
        this.y = y;
        this.health = initialHealth;
        this.maxHealth = initialHealth;
        this.destructionProgress = 0f;
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
        destructionProgress = 1f - (health / maxHealth);
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public float getDestructionProgress() {
        return destructionProgress;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public abstract String getMaterial();
}
