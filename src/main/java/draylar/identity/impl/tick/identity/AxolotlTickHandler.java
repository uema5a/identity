package draylar.identity.impl.tick.identity;

import draylar.identity.Identity;
import draylar.identity.api.IdentityTickHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class AxolotlTickHandler implements IdentityTickHandler<Axolotl> {

    private static final ResourceKey<DamageType> GRASS_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Identity.MODID, "axolotl_grass")
    );

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
                var holder = level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(GRASS_DAMAGE);
                player.hurt(new DamageSource(holder), 1.0F);
            }
        }
    }
}
