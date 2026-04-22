package draylar.identity.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import draylar.identity.Identity;
import draylar.identity.api.IdentityRenderCache;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into {@link LivingEntityRenderer}'s {@code submit} to redirect
 * rendering of a player that has an identity to the identity entity's renderer.
 *
 * <p>This intentionally DOES NOT clear the cache after use. Each extract pass
 * from {@link PlayerEntityRendererMixin} overwrites cache for the next frame,
 * or clears it when identity becomes null. Not clearing here means inventory
 * preview / subsequent sub-passes in the same frame can also render the identity.
 *
 * <p>Identity lookup is fresh via {@code PlayerIdentity.getIdentity(cachedPlayer)},
 * so an identity swap that happens between extract and submit is reflected.
 */
@SuppressWarnings("rawtypes")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntitySubmitMixin extends EntityRenderer {

    private LivingEntitySubmitMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_onSubmit(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        // Only intercept submit calls for player (Avatar) renderers.
        if (!(renderState instanceof AvatarRenderState)) {
            return;
        }

        Player player = IdentityRenderCache.cachedPlayer;
        if (player == null) {
            return;
        }

        // Fetch identity FRESH from the player so swaps between extract and submit are reflected.
        LivingEntity identity = PlayerIdentity.getIdentity(player);
        if (identity == null) {
            return;
        }

        float partialTick = IdentityRenderCache.cachedPartialTick;

        try {
            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

            EntityRenderer identityRenderer = dispatcher.getRenderer(identity);
            if (identityRenderer == null) {
                return;
            }

            EntityRenderState identityRenderState = identityRenderer.createRenderState(identity, partialTick);

            // Position the identity at the player's location in world-space.
            identityRenderState.x = renderState.x;
            identityRenderState.y = renderState.y;
            identityRenderState.z = renderState.z;
            identityRenderState.distanceToCameraSq = renderState.distanceToCameraSq;

            identityRenderer.submit(identityRenderState, poseStack, collector, cameraState);

            boolean showThisPlayerNametag = player != mc.player || IdentityConfig.getInstance().shouldRenderOwnNameTag();
            if (IdentityConfig.getInstance().showPlayerNametag() && showThisPlayerNametag) {
                this.submitNameDisplay(renderState, poseStack, collector, cameraState);
            }

            ci.cancel();
        } catch (Throwable t) {
            Identity.LOGGER.warn("[Identity] LivingEntitySubmitMixin failed, falling back to vanilla rendering", t);
        }
    }
}
