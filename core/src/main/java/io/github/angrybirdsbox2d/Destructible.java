package io.github.angrybirdsbox2d;

public interface Destructible {
    void takeDamage(int damage);
    boolean isDestroyed();
}
