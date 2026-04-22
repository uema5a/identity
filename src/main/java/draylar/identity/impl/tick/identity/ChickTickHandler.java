package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;

public class ChickTickHandler implements IdentityTickHandler<Chicken> {

    public static final Identifier SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "chick_speed");

    @Override
    public void tick(Player player, Chicken chicken) {
        if (player.level().isClientSide()) return;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);

        if (!chicken.isBaby()) {
            if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
            if (maxHealth != null && maxHealth.getBaseValue() != 20.0) {
                maxHealth.setBaseValue(20.0);
            }
            return;
        }

        if (speed != null && speed.getModifier(SPEED_MODIFIER_ID) == null) {
            speed.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    0.5,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
        if (maxHealth != null && maxHealth.getBaseValue() != 10.0) {
            maxHealth.setBaseValue(10.0);
            if (player.getHealth() > 10f) player.setHealth(10f);
        }
    }
}
