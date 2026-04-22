package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;

public class BeeTickHandler implements IdentityTickHandler<Bee> {

    @Override
    public void tick(Player player, Bee bee) {
        if (player.level().isClientSide()) return;
        if (player.tickCount % 40 == 0) {
            if (player.isInWater() || player.level().isRainingAt(player.blockPosition())) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false));
            }
        }
    }
}
