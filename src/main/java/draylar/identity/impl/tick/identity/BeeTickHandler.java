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

    // -15% ground speed (matches vanilla Slowness I) applied while wet.
    public static final Identifier SLOWNESS_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("identity", "bee_wet_slowness");

    // Fraction of configured fly speed while wet.
    private static final float WET_FLYING_SPEED_FACTOR = 0.5F;

    @Override
    public void tick(Player player, Bee bee) {
        boolean wet = player.isInWater() || player.level().isRainingAt(player.blockPosition());
        var abilities = player.getAbilities();

        // Flying speed write: run on BOTH sides so wet→dry transitions feel instantaneous on the
        // local client too. Server-side write still syncs via onUpdateAbilities as the source of truth.
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

        // Attribute modifiers are server-authoritative; bail on client for the rest.
        if (player.level().isClientSide()) return;

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

        // Sprint block is handled by BeeSprintPreventionMixin on LivingEntity.setSprinting —
        // per-tick setSprinting(false) doesn't work because sprint is client-authoritative.
    }
}
