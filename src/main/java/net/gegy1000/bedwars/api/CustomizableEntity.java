package net.gegy1000.bedwars.api;

import net.gegy1000.bedwars.entity.CustomEntity;

import javax.annotation.Nullable;

public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
