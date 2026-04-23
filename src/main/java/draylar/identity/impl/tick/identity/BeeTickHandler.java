package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import draylar.identity.config.IdentityConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;

public class BeeTickHandler implements IdentityTickHandler<Bee> {

    // -15% (matches vanilla Slowness I) applied while wet.
    public static final Identifier SLOWNESS_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "bee_wet_slowness");

    // Default flying speed when not wet. Config override applied on identity grant; we restore to it here.
    private static final float WET_FLYING_SPEED_FACTOR = 0.5F;

    @Override
    public void tick(Player player, Bee bee) {
        if (player.level().isClientSide()) return;

        boolean wet = player.isInWater() || player.level().isRainingAt(player.blockPosition());

        // Ground/normal speed: attribute modifier on MOVEMENT_SPEED (affects walking + swimming).
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
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

        // Creative-style flight uses Abilities.flyingSpeed, NOT the MOVEMENT_SPEED attribute, so the
        // modifier above doesn't slow flight. Scale flyingSpeed directly when wet and flying.
        var abilities = player.getAbilities();
        if (abilities.mayfly) {
            float configured = IdentityConfig.getInstance().flySpeed();
            float target = wet ? configured * WET_FLYING_SPEED_FACTOR : configured;
            if (Math.abs(abilities.getFlyingSpeed() - target) > 1.0e-4F) {
                abilities.setFlyingSpeed(target);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    sp.onUpdateAbilities();
                }
            }
        }

        // Disable sprinting while flying as a Bee — bees don't dash in mid-air.
        if (abilities.flying && player.isSprinting()) {
            player.setSprinting(false);
        }
    }
}
