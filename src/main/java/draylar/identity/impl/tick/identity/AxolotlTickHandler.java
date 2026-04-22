package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class AxolotlTickHandler implements IdentityTickHandler<Axolotl> {

    @Override
    public void tick(Player player, Axolotl axolotl) {
        var level = player.level();
        if (level.isClientSide()) return;

        if (player.isInWater()) {
            player.setAirSupply(player.getMaxAirSupply());
        }

        if (player.tickCount % 20 == 0 && player.onGround()) {
            var below = player.blockPosition().below();
            if (level.getBlockState(below).is(Blocks.GRASS_BLOCK)) {
                player.hurt(player.damageSources().hotFloor(), 1.0F);
            }
        }
    }
}
