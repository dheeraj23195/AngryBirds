package io.github.angrybirdsbox2d;

public class SteelBlock extends Block {
    public SteelBlock(float x, float y) {
        super(x, y, 20);
    }

    @Override
    public String getMaterial() {
        return "steel";
    }
}
