package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.game.upgrade.PlayerUpgrades;
import net.gegy1000.bedwars.game.upgrade.UpgradeType;
import net.gegy1000.gl.game.GameTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

public final class BwParticipant {
    private final ServerWorld world;
    public final UUID playerId;
    public final GameTeam team;

    public final PlayerUpgrades upgrades;

    BwMap.TeamSpawn respawningAt;
    long respawnTime = -1;
    boolean eliminated;

    BwParticipant(BwActive game, ServerPlayerEntity player, GameTeam team) {
        this.world = player.getServerWorld();
        this.playerId = player.getUuid();
        this.team = team;

        this.upgrades = new PlayerUpgrades(game, this);

        this.upgrades.addAt(UpgradeType.ARMOR, 0);
        this.upgrades.addAt(UpgradeType.SWORD, 0);
        this.upgrades.add(UpgradeType.PICKAXE);
        this.upgrades.add(UpgradeType.AXE);
        this.upgrades.add(UpgradeType.SHEARS);
    }

    public void startRespawning(BwMap.TeamSpawn spawn) {
        this.respawnTime = this.world.getTime() + BwActive.RESPAWN_TICKS;
        this.respawningAt = spawn;
    }

    public void stopRespawning() {
        this.respawningAt = null;
        this.respawnTime = -1;
    }

    public boolean isRespawning() {
        return this.respawningAt != null;
    }

    @Nullable
    public ServerPlayerEntity player() {
        return this.world.getServer().getPlayerManager().getPlayer(this.playerId);
    }

    public boolean inGame() {
        return this.player() != null;
    }
}
