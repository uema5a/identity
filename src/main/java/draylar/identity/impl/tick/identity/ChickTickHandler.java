package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;

public class ChickTickHandler implements IdentityTickHandler<Chicken> {

    // +50% movement speed (Speed II equivalent).
    public static final Identifier SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "chick_speed");
    // -10 max HP (halves 20 → 10). Uses a modifier so swap cleanup can remove it cleanly;
    // writing baseValue directly would persist across swaps if the cleanup path never fires.
    public static final Identifier MAX_HEALTH_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "chick_max_health");

    @Override
    public void tick(Player player, Chicken chicken) {
        if (player.level().isClientSide()) return;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);

        if (!chicken.isBaby()) {
            if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
            if (maxHealth != null) maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
            return;
        }

        if (speed != null && speed.getModifier(SPEED_MODIFIER_ID) == null) {
            speed.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    0.5,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
        if (maxHealth != null && maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID) == null) {
            maxHealth.addTransientModifier(new AttributeModifier(
                    MAX_HEALTH_MODIFIER_ID,
                    -10.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
            // Clip current health so the HUD doesn't show hearts above the new max.
            if (player.getHealth() > (float) maxHealth.getValue()) {
                player.setHealth((float) maxHealth.getValue());
            }
        }
    }
}
