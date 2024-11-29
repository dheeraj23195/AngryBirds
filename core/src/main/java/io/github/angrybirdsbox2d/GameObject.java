package io.github.angrybirdsbox2d;

public interface GameObject {
    float getX();
    float getY();
    void setX(float x);
    void setY(float y);
    void takeDamage(int damage);
    float getRotation(); // Add this
    void setRotation(float rotation);
}
