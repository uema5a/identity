package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;

public class CowTickHandler implements IdentityTickHandler<Cow> {

    // +20% ADD_MULTIPLIED_TOTAL. Vanilla sprint already adds +30%, so while sprinting
    // the total movement speed is (1 + 0.30 + 0.20) × base = 1.50× base.
    public static final Identifier SPRINT_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "cow_sprint_speed");

    @Override
    public void tick(Player player, Cow cow) {
        if (player.level().isClientSide()) return;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        if (player.isSprinting()) {
            if (speed.getModifier(SPRINT_SPEED_MODIFIER_ID) == null) {
                speed.addTransientModifier(new AttributeModifier(
                        SPRINT_SPEED_MODIFIER_ID,
                        0.20,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        } else {
            speed.removeModifier(SPRINT_SPEED_MODIFIER_ID);
        }
    }
}
