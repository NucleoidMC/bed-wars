package xyz.nucleoid.bedwars.game.active;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.upgrade.PlayerUpgrades;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.util.PlayerRef;

public final class BwParticipant {
    private final ServerWorld world;
    public final PlayerRef ref;
    public final GameTeam team;
    public final GameTeamConfig teamConfig;

    public final PlayerUpgrades upgrades;

    AttackRecord lastAttack;

    BwMap.TeamSpawn respawningAt;
    long respawnTime = -1;
    boolean eliminated;

    BwParticipant(BwActive game, ServerPlayerEntity player, GameTeam team, GameTeamConfig teamConfig) {
        this.world = player.getServerWorld();
        this.ref = PlayerRef.of(player);
        this.team = team;
        this.teamConfig = teamConfig;

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
        return this.ref.getEntity(this.world);
    }

    public boolean isAlive() {
        return !this.eliminated && this.isOnline();
    }

    public boolean isOnline() {
        return this.ref.isOnline(this.world);
    }
}
