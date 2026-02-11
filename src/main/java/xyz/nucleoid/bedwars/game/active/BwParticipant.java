package xyz.nucleoid.bedwars.game.active;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.upgrade.PlayerUpgrades;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public final class BwParticipant {
    private final ServerLevel world;
    public final PlayerRef ref;
    public final GameTeam team;

    public final PlayerUpgrades upgrades;

    AttackRecord lastAttack;

    BwMap.TeamSpawn respawningAt;
    long respawnTime = -1;
    boolean eliminated;

    BwParticipant(BwActive game, ServerPlayer player, GameTeam team) {
        this.world = player.level();
        this.ref = PlayerRef.of(player);
        this.team = team;

        this.upgrades = new PlayerUpgrades(game, this);

        this.upgrades.addAt(UpgradeType.ARMOR, 0);
        this.upgrades.addAt(UpgradeType.SWORD, 0);
        this.upgrades.add(UpgradeType.SPEAR);
        this.upgrades.add(UpgradeType.PICKAXE);
        this.upgrades.add(UpgradeType.AXE);
        this.upgrades.add(UpgradeType.SHEARS);
    }

    public void startRespawning(BwMap.TeamSpawn spawn) {
        this.respawnTime = this.world.getGameTime() + BwActive.RESPAWN_TICKS;
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
    public ServerPlayer player() {
        return this.ref.getEntity(this.world);
    }

    public boolean isAlive() {
        return !this.eliminated && this.isOnline();
    }

    public boolean isOnline() {
        return this.ref.isOnline(this.world);
    }
}
