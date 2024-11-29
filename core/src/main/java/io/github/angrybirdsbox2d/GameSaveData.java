package io.github.angrybirdsbox2d;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<LevelSingle> levelsList;
    private final Date saveDate;
    private final int currentLevelNumber;

    public GameSaveData(List<LevelSingle> levels, int currentLevel) {
        this.levelsList = new ArrayList<>(levels);  // Create copy of levels
        this.currentLevelNumber = currentLevel;
        this.saveDate = new Date();  // Current date/time
    }

    public List<LevelSingle> getLevelsList() { return levelsList; }
    public Date getSaveDate() { return saveDate; }
    public int getCurrentLevelNumber() { return currentLevelNumber; }
}
