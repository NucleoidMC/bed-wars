package net.gegy1000.gl.game.map;

import javax.annotation.Nullable;

public interface MapViewer {
    void setViewing(StagingMap map);

    @Nullable
    StagingMap getViewing();
}
