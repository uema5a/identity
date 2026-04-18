package draylar.identity.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IdentityTickHandler<Z extends Entity> {

    void tick(Player player, Z entity);
}
