package net.gegy1000.bedwars.api;

import net.gegy1000.bedwars.game.map.StagingMap;

import javax.annotation.Nullable;

public interface MapViewer {
    void setViewing(StagingMap map);

    @Nullable
    StagingMap getViewing();
}
