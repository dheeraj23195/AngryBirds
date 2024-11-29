package io.github.angrybirdsbox2d;

public class WoodBlock extends Block {
    public WoodBlock(float x, float y) {
        super(x, y, 12.5f);
    }

    @Override
    public String getMaterial() {
        return "wood";
    }
}
