package net.gegy1000.gl.entity;

import javax.annotation.Nullable;

public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
