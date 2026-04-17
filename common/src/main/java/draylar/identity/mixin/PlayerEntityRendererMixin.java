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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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

    /**
     * After the normal render state extraction, sync player data to the identity entity
     * and cache the identity for use in the submit method.
     */
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void identity_onExtractRenderState(Avatar avatarEntity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!(avatarEntity instanceof Player player)) {
            IdentityRenderCache.cachedIdentity = null;
            IdentityRenderCache.cachedPlayer = null;
            return;
        }

        LivingEntity identity = PlayerIdentity.getIdentity(player);
        if (identity == null) {
            IdentityRenderCache.cachedIdentity = null;
            IdentityRenderCache.cachedPlayer = null;
            return;
        }

        IdentityRenderCache.cachedPlayer = player;
        IdentityRenderCache.cachedPartialTick = partialTick;

        // Sync player data to identity entity
        identity_syncPlayerToIdentity(player, identity);

        // Update identity-specific properties
        @SuppressWarnings("unchecked")
        EntityUpdater<LivingEntity> entityUpdater = (EntityUpdater<LivingEntity>) EntityUpdaters.getUpdater((EntityType<? extends LivingEntity>) identity.getType());
        if (entityUpdater != null) {
            entityUpdater.update(player, identity);
        }

        IdentityRenderCache.cachedIdentity = identity;
    }

    /**
     * Syncs relevant player data to the identity entity so animations and state are correct.
     */
    @Unique
    private void identity_syncPlayerToIdentity(Player player, LivingEntity identity) {
        // Sync walk animation
        LimbAnimatorAccessor target = (LimbAnimatorAccessor) identity.walkAnimation;
        LimbAnimatorAccessor source = (LimbAnimatorAccessor) player.walkAnimation;
        target.setPrevSpeed(source.getPrevSpeed());
        target.setSpeed(source.getSpeed());
        target.setPos(source.getPos());

        // Sync swing animation
        identity.swinging = player.swinging;
        identity.swingTime = player.swingTime;
        identity.oAttackAnim = player.oAttackAnim;
        identity.attackAnim = player.attackAnim;

        // Sync body/head rotation
        identity.yBodyRot = player.yBodyRot;
        identity.yBodyRotO = player.yBodyRotO;
        identity.yHeadRot = player.yHeadRot;
        identity.yHeadRotO = player.yHeadRotO;

        // Sync age/tick count
        identity.tickCount = player.tickCount;

        // Sync swinging arm
        identity.swingingArm = player.swingingArm;

        // Sync ground state and movement
        identity.setOnGround(player.onGround());
        identity.setDeltaMovement(player.getDeltaMovement());

        // Sync vehicle and water state
        ((EntityAccessor) identity).setVehicle(player.getVehicle());
        ((EntityAccessor) identity).setWasTouchingWater(player.isInWater());

        // Sync pitch (phantoms have inverted pitch)
        if (identity instanceof Phantom) {
            identity.setXRot(-player.getXRot());
            identity.xRotO = -player.xRotO;
        } else {
            identity.setXRot(player.getXRot());
            identity.xRotO = player.xRotO;
        }

        // Equip held items on identity
        if (IdentityConfig.getInstance().identitiesEquipItems()) {
            identity.setItemSlot(EquipmentSlot.MAINHAND, player.getItemBySlot(EquipmentSlot.MAINHAND));
            identity.setItemSlot(EquipmentSlot.OFFHAND, player.getItemBySlot(EquipmentSlot.OFFHAND));
        }

        // Equip armor items on identity
        if (IdentityConfig.getInstance().identitiesEquipArmor()) {
            identity.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
            identity.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
            identity.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
            identity.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
        }

        // Set attacking flag on mobs
        if (identity instanceof Mob mob) {
            mob.setAggressive(player.isUsingItem());
        }

        // Assign pose
        identity.setPose(player.getPose());

        // Set active hand after configuring held items
        InteractionHand hand = player.getUsedItemHand() == null ? InteractionHand.MAIN_HAND : player.getUsedItemHand();
        identity.startUsingItem(hand);
        ((LivingEntityAccessor) identity).callSetLivingEntityFlag(1, player.isUsingItem());
        identity.getTicksUsingItem();
        ((LivingEntityAccessor) identity).callTickActiveItemStack();
    }

    /**
     * For TameableEntity identities, use the parent class's position offset
     * to avoid the sitting offset that TameableEntity renderers apply.
     */
    @Inject(
            method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_modifyPositionOffset(AvatarRenderState state, CallbackInfoReturnable<Vec3> cir) {
        if (IdentityRenderCache.cachedIdentity != null && IdentityRenderCache.cachedIdentity instanceof TamableAnimal) {
            cir.setReturnValue(super.getRenderOffset(state));
        }
    }

    /**
     * Intercept right hand rendering for first-person view.
     * If the player has an identity, render the identity's arm instead.
     */
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
        if (identity_renderHandForIdentity(identity, poseStack, collector, light, true)) {
            ci.cancel();
        }
    }

    /**
     * Intercept left hand rendering for first-person view.
     * If the player has an identity, render the identity's arm instead.
     */
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
        if (identity_renderHandForIdentity(identity, poseStack, collector, light, false)) {
            ci.cancel();
        }
    }

    /**
     * Renders the identity's first-person hand (arm + optional sleeve) using the MC 26.1 submit API.
     * Returns true if the identity arm was rendered (caller should cancel the vanilla arm render).
     */
    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean identity_renderHandForIdentity(LivingEntity identity, PoseStack poseStack, SubmitNodeCollector collector, int light, boolean rightHand) {
        EntityRenderer<?, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(identity);
        if (!(renderer instanceof LivingEntityRenderer livingRenderer)) return false;

        EntityModel model = livingRenderer.getModel();

        ModelPart arm = null;
        ModelPart sleeve = null;

        if (model instanceof PlayerModel playerModel) {
            arm = rightHand ? playerModel.rightArm : playerModel.leftArm;
            sleeve = rightHand ? playerModel.rightSleeve : playerModel.leftSleeve;
        } else if (model instanceof HumanoidModel humanoidModel) {
            arm = rightHand ? humanoidModel.rightArm : humanoidModel.leftArm;
        } else {
            Pair<ModelPart, ArmRenderingManipulator<?>> pair = EntityArms.get(identity, model);
            if (pair != null) {
                arm = pair.getFirst();
                ((ArmRenderingManipulator) pair.getSecond()).run(poseStack, model);
                poseStack.translate(0, -.35, .5);
            }
        }

        EntityRenderState renderState = ((EntityRenderer) renderer).createRenderState(identity, 0.0f);
        model.setupAnim(renderState);

        Identifier identityTexture = livingRenderer.getTextureLocation((LivingEntityRenderState) renderState);
        if (identityTexture == null) return arm != null || sleeve != null;

        if (arm != null) {
            arm.xRot = 0.0F;
            collector.submitModelPart(arm, poseStack, model.renderType(identityTexture), light, OverlayTexture.NO_OVERLAY, null);
        }
        if (sleeve != null) {
            sleeve.xRot = 0.0F;
            collector.submitModelPart(sleeve, poseStack, model.renderType(identityTexture), light, OverlayTexture.NO_OVERLAY, null);
        }
        return true;
    }
}
