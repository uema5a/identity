package draylar.identity.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import draylar.identity.api.IdentityRenderCache;
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
 * Mixin into {@link LivingEntityRenderer} to intercept the {@code submit} method,
 * which is defined on {@code LivingEntityRenderer} and NOT overridden by {@code AvatarRenderer}.
 *
 * <p>When a player has an identity, this mixin renders the identity entity instead of the player model
 * and cancels the original player rendering.</p>
 *
 * <p>The cached identity data is written by {@link PlayerEntityRendererMixin} during
 * {@code extractRenderState} and read here during {@code submit}.</p>
 */
@SuppressWarnings("rawtypes")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntitySubmitMixin extends EntityRenderer {

    private LivingEntitySubmitMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    /**
     * Intercept the submit (render) call on LivingEntityRenderer.
     * If the render state belongs to a player with an identity,
     * render the identity entity instead and cancel the original rendering.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_onSubmit(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        // AvatarRenderState is the marker that this submit call is for a player
        if (!(renderState instanceof AvatarRenderState)) {
            return;
        }

        LivingEntity identity = IdentityRenderCache.cachedIdentity;
        Player player = IdentityRenderCache.cachedPlayer;
        float partialTick = IdentityRenderCache.cachedPartialTick;

        if (identity == null || player == null) {
            return;
        }

        IdentityRenderCache.cachedIdentity = null;
        IdentityRenderCache.cachedPlayer = null;

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        EntityRenderer identityRenderer = dispatcher.getRenderer(identity);
        EntityRenderState identityRenderState = identityRenderer.createRenderState(identity, partialTick);

        // Position the identity at the player's location so it renders in the right place
        identityRenderState.x = renderState.x;
        identityRenderState.y = renderState.y;
        identityRenderState.z = renderState.z;
        identityRenderState.distanceToCameraSq = renderState.distanceToCameraSq;

        identityRenderer.submit(identityRenderState, poseStack, collector, cameraState);

        boolean showThisPlayerNametag = player != mc.player || IdentityConfig.getInstance().shouldRenderOwnNameTag();
        if (IdentityConfig.getInstance().showPlayerNametag() && showThisPlayerNametag) {
            // submitNameDisplay dispatches to AvatarRenderer's override which carries the player name
            this.submitNameDisplay(renderState, poseStack, collector, cameraState);
        }

        ci.cancel();
    }
}
