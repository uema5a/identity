package draylar.identity.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import draylar.identity.api.IdentityRenderCache;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.model.ArmRenderingManipulator;
import draylar.identity.api.model.EntityArms;
import draylar.identity.api.model.EntityUpdater;
import draylar.identity.api.model.EntityUpdaters;
import draylar.identity.config.IdentityConfig;
import draylar.identity.mixin.accessor.EntityAccessor;
import draylar.identity.mixin.accessor.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer {

    private PlayerEntityRendererMixin(EntityRendererProvider.Context ctx, EntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void identity_onExtractRenderState(Avatar avatarEntity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        // Only clear cache for non-Player avatars if we had a cached entry from a prior extract; otherwise leave alone.
        if (!(avatarEntity instanceof Player player)) {
            return;
        }

        LivingEntity identity = PlayerIdentity.getIdentity(player);
        if (identity == null) {
            IdentityRenderCache.cachedIdentity = null;
            IdentityRenderCache.cachedPlayer = null;
            return;
        }

        try {

        IdentityRenderCache.cachedPlayer = player;
        IdentityRenderCache.cachedPartialTick = partialTick;
        identity_syncPlayerToIdentity(player, identity);

        @SuppressWarnings("unchecked")
        EntityUpdater<LivingEntity> entityUpdater = (EntityUpdater<LivingEntity>) EntityUpdaters.getUpdater((EntityType<? extends LivingEntity>) identity.getType());
        if (entityUpdater != null) {
            entityUpdater.update(player, identity);
        }

        IdentityRenderCache.cachedIdentity = identity;
        } catch (Throwable t) {
            draylar.identity.Identity.LOGGER.warn("[Identity] extractRenderState failed", t);
        }
    }

    @Unique
    private void identity_syncPlayerToIdentity(Player player, LivingEntity identity) {
        LimbAnimatorAccessor target = (LimbAnimatorAccessor) identity.walkAnimation;
        LimbAnimatorAccessor source = (LimbAnimatorAccessor) player.walkAnimation;
        target.setPrevSpeed(source.getPrevSpeed());
        target.setSpeed(source.getSpeed());
        target.setPos(source.getPos());

        identity.swinging = player.swinging;
        identity.swingTime = player.swingTime;
        identity.oAttackAnim = player.oAttackAnim;
        identity.attackAnim = player.attackAnim;

        identity.yBodyRot = player.yBodyRot;
        identity.yBodyRotO = player.yBodyRotO;
        identity.yHeadRot = player.yHeadRot;
        identity.yHeadRotO = player.yHeadRotO;
        identity.tickCount = player.tickCount;
        identity.swingingArm = player.swingingArm;

        identity.setOnGround(player.onGround());
        identity.setDeltaMovement(player.getDeltaMovement());
        ((EntityAccessor) identity).setVehicle(player.getVehicle());
        ((EntityAccessor) identity).setWasTouchingWater(player.isInWater());

        // Phantoms are rendered upside-down, so pitch must be inverted
        if (identity instanceof Phantom) {
            identity.setXRot(-player.getXRot());
            identity.xRotO = -player.xRotO;
        } else {
            identity.setXRot(player.getXRot());
            identity.xRotO = player.xRotO;
        }

        if (IdentityConfig.getInstance().identitiesEquipItems()) {
            identity.setItemSlot(EquipmentSlot.MAINHAND, player.getItemBySlot(EquipmentSlot.MAINHAND));
            identity.setItemSlot(EquipmentSlot.OFFHAND, player.getItemBySlot(EquipmentSlot.OFFHAND));
        }

        if (IdentityConfig.getInstance().identitiesEquipArmor()) {
            identity.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
            identity.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
            identity.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
            identity.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
        }

        if (identity instanceof Mob mob) {
            mob.setAggressive(player.isUsingItem());
        }

        identity.setPose(player.getPose());

        // startUsingItem before tickActiveItemStack so item-use timer advances correctly
        InteractionHand hand = player.getUsedItemHand() == null ? InteractionHand.MAIN_HAND : player.getUsedItemHand();
        identity.startUsingItem(hand);
        ((LivingEntityAccessor) identity).callSetLivingEntityFlag(1, player.isUsingItem());
        identity.getTicksUsingItem();
        ((LivingEntityAccessor) identity).callTickActiveItemStack();
    }

    @Inject(
            method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_modifyPositionOffset(AvatarRenderState state, CallbackInfoReturnable<Vec3> cir) {
        // TamableAnimal renderers apply a sitting offset; bypass it so the player stays at ground level
        if (IdentityRenderCache.cachedIdentity instanceof TamableAnimal) {
            cir.setReturnValue(super.getRenderOffset(state));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "renderRightHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_onRenderRightHand(PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier texture, boolean slim, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        LivingEntity identity = PlayerIdentity.getIdentity(mc.player);
        if (identity == null) return;

        EntityRenderer<?, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(identity);
        if (!(renderer instanceof LivingEntityRenderer livingRenderer)) return;

        EntityModel model = livingRenderer.getModel();
        ModelPart arm = null;
        ModelPart sleeve = null;

        if (model instanceof PlayerModel playerModel) {
            arm = playerModel.rightArm;
            sleeve = playerModel.rightSleeve;
        } else if (model instanceof HumanoidModel humanoidModel) {
            arm = humanoidModel.rightArm;
        } else {
            Pair<ModelPart, ArmRenderingManipulator<?>> pair = EntityArms.get(identity, model);
            if (pair != null) {
                arm = pair.getFirst();
                ((ArmRenderingManipulator) pair.getSecond()).run(poseStack, model);
                poseStack.translate(0, -.35, .5);
            }
        }

        identity_submitArmParts(poseStack, collector, light, identity, renderer, livingRenderer, model, arm, sleeve);
        ci.cancel();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "renderLeftHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_onRenderLeftHand(PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier texture, boolean slim, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        LivingEntity identity = PlayerIdentity.getIdentity(mc.player);
        if (identity == null) return;

        EntityRenderer<?, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(identity);
        if (!(renderer instanceof LivingEntityRenderer livingRenderer)) return;

        EntityModel model = livingRenderer.getModel();
        ModelPart arm = null;
        ModelPart sleeve = null;

        if (model instanceof PlayerModel playerModel) {
            arm = playerModel.leftArm;
            sleeve = playerModel.leftSleeve;
        } else if (model instanceof HumanoidModel humanoidModel) {
            arm = humanoidModel.leftArm;
        } else {
            // Non-humanoid models only expose a single arm provider; left arm support is limited
            Pair<ModelPart, ArmRenderingManipulator<?>> pair = EntityArms.get(identity, model);
            if (pair != null) {
                arm = pair.getFirst();
                ((ArmRenderingManipulator) pair.getSecond()).run(poseStack, model);
                poseStack.translate(0, -.35, .5);
            }
        }

        identity_submitArmParts(poseStack, collector, light, identity, renderer, livingRenderer, model, arm, sleeve);
        ci.cancel();
    }

    /**
     * Animates the identity's model and submits arm + optional sleeve for first-person rendering.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Unique
    private void identity_submitArmParts(
            PoseStack poseStack, SubmitNodeCollector collector, int light,
            LivingEntity identity, EntityRenderer<?, ?> renderer,
            LivingEntityRenderer livingRenderer, EntityModel model,
            ModelPart arm, ModelPart sleeve) {

        EntityRenderState renderState = ((EntityRenderer) renderer).createRenderState(identity, 0.0f);
        model.setupAnim(renderState);

        Identifier identityTexture = livingRenderer.getTextureLocation((LivingEntityRenderState) renderState);
        if (identityTexture == null) return;

        if (arm != null) {
            arm.xRot = 0.0F;
            collector.submitModelPart(arm, poseStack, model.renderType(identityTexture), light, OverlayTexture.NO_OVERLAY, null);
        }
        if (sleeve != null) {
            sleeve.xRot = 0.0F;
            collector.submitModelPart(sleeve, poseStack, model.renderType(identityTexture), light, OverlayTexture.NO_OVERLAY, null);
        }
    }
}
