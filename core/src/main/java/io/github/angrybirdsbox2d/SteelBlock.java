package io.github.angrybirdsbox2d;

import java.io.Serializable;

public class SteelBlock extends Block implements Serializable {
    private static final long serialVersionUID = 2L;
    public SteelBlock(float x, float y) {
        super(x, y, 20);
    }

    @Override
    public String getMaterial() {
        return "steel";
    }
}
