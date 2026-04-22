package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;

public class BeeTickHandler implements IdentityTickHandler<Bee> {

    public static final Identifier SLOWNESS_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "bee_wet_slowness");

    @Override
    public void tick(Player player, Bee bee) {
        if (player.level().isClientSide()) return;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        boolean wet = player.isInWater() || player.level().isRainingAt(player.blockPosition());
        if (wet) {
            if (speed.getModifier(SLOWNESS_MODIFIER_ID) == null) {
                speed.addTransientModifier(new AttributeModifier(
                        SLOWNESS_MODIFIER_ID,
                        -0.15,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        } else {
            speed.removeModifier(SLOWNESS_MODIFIER_ID);
        }
    }
}
