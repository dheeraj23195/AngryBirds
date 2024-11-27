package io.github.angrybirdsbox2d;

public class CollisionBox {
    public float x, y, width, height;
    public CollisionType type;

    public CollisionBox(float x, float y, float width, float height, CollisionType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public boolean overlaps(CollisionBox other) {
        return x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y;
    }

    public void updatePosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }
}

