package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;

public class ChickTickHandler implements IdentityTickHandler<Chicken> {

    @Override
    public void tick(Player player, Chicken chicken) {
        if (player.level().isClientSide()) return;
        if (!chicken.isBaby()) {
            restoreMaxHealth(player);
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, true, false));
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getBaseValue() != 10.0) {
            maxHealth.setBaseValue(10.0);
            if (player.getHealth() > 10f) player.setHealth(10f);
        }
    }

    private void restoreMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getBaseValue() != 20.0) {
            maxHealth.setBaseValue(20.0);
        }
    }
}
