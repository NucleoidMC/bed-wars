package net.gegy1000.bedwars.custom;

import javax.annotation.Nullable;

public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
