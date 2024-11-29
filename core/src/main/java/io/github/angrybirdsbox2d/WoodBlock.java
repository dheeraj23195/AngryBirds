package io.github.angrybirdsbox2d;

import java.io.Serializable;

public class WoodBlock extends Block implements Serializable {
    private static final long serialVersionUID = 3L;

    public WoodBlock(float x, float y) {
        super(x, y, 12.5f);
    }

    @Override
    public String getMaterial() {
        return "wood";
    }
}
