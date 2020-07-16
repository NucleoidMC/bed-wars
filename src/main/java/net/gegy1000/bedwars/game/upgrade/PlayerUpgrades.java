package net.gegy1000.bedwars.game.upgrade;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.bedwars.game.BwState;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

// TODO: this code is all horrible, i hate it.
public final class PlayerUpgrades {
    private final BedWars game;
    private final BwState.Participant participant;

    private final Object2IntMap<UpgradeType<?>> map = new Object2IntLinkedOpenHashMap<>();
    private final Set<UpgradeType<?>> typesToDowngrade = new HashSet<>();

    public PlayerUpgrades(BedWars game, BwState.Participant participant) {
        this.game = game;
        this.participant = participant;
        this.map.defaultReturnValue(-1);
    }

    public void applyAll() {
        ServerPlayerEntity player = this.participant.player();
        if (player == null) {
            return;
        }

        for (Object2IntMap.Entry<UpgradeType<?>> entry : Object2IntMaps.fastIterable(this.map)) {
            UpgradeType<?> type = entry.getKey();
            int level = entry.getIntValue();

            Upgrade upgrade = type.forLevel(level);
            if (upgrade != null) {
                upgrade.applyTo(this.game, player, this.participant);
            }
        }
    }

    public <T extends Upgrade> void applyLevel(UpgradeType<T> type, int level) {
        if (!type.canRemove() && level < 0) {
            return;
        }

        int lastLevel = this.setLevel(type, level);

        ServerPlayerEntity player = this.participant.player();
        if (player != null) {
            T lastUpgrade = type.forLevel(lastLevel);
            if (lastUpgrade != null) {
                lastUpgrade.removeFrom(this.game, player);
            }

            T upgrade = type.forLevel(level);
            if (upgrade != null) {
                upgrade.applyTo(this.game, player, this.participant);
            }
        }
    }

    public void onDeath() {
        for (UpgradeType<?> downgradeType : this.typesToDowngrade) {
            int currentLevel = this.getLevel(downgradeType);
            this.applyLevel(downgradeType, currentLevel - 1);
        }
    }

    public int setLevel(UpgradeType<?> type, int level) {
        if (level >= 0 && type.shouldDowngradeOnDeath()) {
            this.typesToDowngrade.add(type);
        } else {
            this.typesToDowngrade.remove(type);
        }

        return this.map.put(type, level);
    }

    public <T extends Upgrade> int getLevel(UpgradeType<T> type) {
        return this.map.getInt(type);
    }

    public <T extends Upgrade> T get(UpgradeType<T> type) {
        int level = this.map.getInt(type);
        return type.forLevel(level);
    }
}
