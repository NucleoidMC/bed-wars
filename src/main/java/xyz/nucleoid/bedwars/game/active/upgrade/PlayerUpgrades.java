package xyz.nucleoid.bedwars.game.active.upgrade;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerUpgrades {
    private final BwActive game;
    private final BwParticipant participant;

    private final ObjectSet<UpgradeType<?>> upgradeTypes = new ObjectOpenHashSet<>();
    private final Object2IntMap<UpgradeType<?>> map = new Object2IntLinkedOpenHashMap<>();

    public PlayerUpgrades(BwActive game, BwParticipant participant) {
        this.game = game;
        this.participant = participant;
        this.map.defaultReturnValue(-1);
    }

    public void add(UpgradeType<?> type) {
        this.upgradeTypes.add(type);
    }

    public void addAt(UpgradeType<?> type, int level) {
        this.upgradeTypes.add(type);
        this.map.put(type, level);
    }

    public void applyAll() {
        ServerPlayerEntity player = this.participant.player();
        if (player == null) {
            return;
        }

        for (UpgradeType<?> type : this.upgradeTypes) {
            int level = this.map.getInt(type);

            Upgrade upgrade = type.forLevel(level);
            if (upgrade != null) {
                upgrade.removeFrom(this.game, player);
                upgrade.applyTo(this.game, player, this.participant);
            }
        }
    }

    public <T extends Upgrade> void applyLevel(UpgradeType<T> type, int level) {
        if (!this.upgradeTypes.contains(type)) {
            return;
        }

        int lastLevel = this.map.put(type, level);

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

    public void tryDowngrade(UpgradeType<?> type) {
        int currentLevel = this.getLevel(type);
        if (currentLevel > 0) {
            this.applyLevel(type, currentLevel - 1);
        }
    }

    public <T extends Upgrade> int getLevel(UpgradeType<T> type) {
        return this.map.getInt(type);
    }

    public <T extends Upgrade> T get(UpgradeType<T> type) {
        int level = this.map.getInt(type);
        return type.forLevel(level);
    }
}
