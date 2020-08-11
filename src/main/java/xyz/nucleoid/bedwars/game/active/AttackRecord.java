package xyz.nucleoid.bedwars.game.active;

import xyz.nucleoid.plasmid.util.PlayerRef;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AttackRecord {
    public static final long EXPIRE_TIME = 20 * 5;

    public final PlayerRef player;
    private final long expireTime;

    public AttackRecord(PlayerRef player, long time) {
        this.player = player;
        this.expireTime = time + EXPIRE_TIME;
    }

    public static AttackRecord fromAttacker(ServerPlayerEntity player) {
        return new AttackRecord(PlayerRef.of(player), player.world.getTime());
    }

    public boolean isValid(long time) {
        return time < this.expireTime;
    }
}
