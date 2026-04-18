package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import draylar.identity.config.IdentityConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

public class WardenTickHandler implements IdentityTickHandler<Warden> {

    @Override
    public void tick(Player player, Warden entity) {
        if(!player.level().isClientSide()) {
            if(player.tickCount % 20 == 0) {

                // Blind the Warden Identity player.
                if(IdentityConfig.getInstance().wardenIsBlinded()) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 3, 0, true, false));
                }

                // TODO: Port nearby player blinding to 26.1 - Level.getPlayers(TargetingConditions) removed
            }
        }
    }
}
