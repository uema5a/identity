package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.mixin.accessor.EntityShadowAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Scales the player shadow to match the identity entity's shadow radius.
 *
 * <p>In MC 26.1 the shadow radius is baked into {@link EntityRenderState#shadowRadius} during
 * {@link EntityRenderer#finalizeRenderState}, which calls {@code extractShadow}.
 * We overwrite that field at TAIL of {@code finalizeRenderState} so the identity radius is used
 * for all downstream shadow rendering without touching the shadow geometry path.</p>
 */
@Mixin(EntityRenderer.class)
public class ShadowMixin {

    @Inject(
            method = "finalizeRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V",
            at = @At("TAIL")
    )
    private void identity_scaleShadow(Entity entity, EntityRenderState state, CallbackInfo ci) {
        if (!(entity instanceof Player player)) {
            return;
        }

        LivingEntity identity = PlayerIdentity.getIdentity(player);
        if (identity == null) {
            return;
        }

        EntityRenderer<?, ?> identityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(identity);
        float shadowRadius = ((EntityShadowAccessor) identityRenderer).getShadowRadius();
        state.shadowRadius = shadowRadius * (identity.isBaby() ? 0.5f : 1.0f);
    }
}
