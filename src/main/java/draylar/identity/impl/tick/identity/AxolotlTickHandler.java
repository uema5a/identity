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

    private static boolean loggedMissingDamageType = false;

    private static void logMissingOnce() {
        if (!loggedMissingDamageType) {
            loggedMissingDamageType = true;
            Identity.LOGGER.warn("Identity damage_type 'axolotl_grass' not in registry; grass damage disabled");
        }
    }

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
                var registry = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
                var holder = registry.get(GRASS_DAMAGE);
                if (holder.isPresent()) {
                    player.hurt(new DamageSource(holder.get()), 1.0F);
                } else {
                    logMissingOnce();
                }
            }
        }
    }
}
