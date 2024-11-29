package io.github.angrybirdsbox2d;

public class GlassBlock extends Block {
    public GlassBlock(float x, float y) {
        super(x, y, 6.25f);
    }

    @Override
    public String getMaterial() {
        return "glass";
    }
}
