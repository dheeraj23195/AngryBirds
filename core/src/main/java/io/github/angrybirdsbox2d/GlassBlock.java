package io.github.angrybirdsbox2d;

import java.io.Serializable;

public class GlassBlock extends Block implements Serializable {
    private static final long serialVersionUID = 4L;

    public GlassBlock(float x, float y) {
        super(x, y, 6.25f);
    }

    @Override
    public String getMaterial() {
        return "glass";
    }
}
