package draylar.identity.mixin;

import draylar.identity.Identity;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.mixin.accessor.*;
import draylar.identity.registry.IdentityEntityTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {

    @Shadow public abstract boolean isSpectator();
    @Shadow public abstract boolean isSwimming();

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    // getDefaultDimensions inject is in AvatarDimensionsMixin (targets Avatar where the method is declared)

    @Inject(method = "tick", at = @At("HEAD"))
    private void identity$loadForcedIdentity(CallbackInfo ci) {
        if((Object) this instanceof ServerPlayer serverPlayerEntity) {
            @Nullable LivingEntity active = PlayerIdentity.getIdentity(serverPlayerEntity);
            if(active == null) {
                @Nullable String forced = IdentityConfig.getInstance().getForcedIdentity();
                if(forced != null) {
                    EntityType foundType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(forced));
                    if(foundType != null) {
                        PlayerIdentity.updateIdentity(serverPlayerEntity, new IdentityType<LivingEntity>(
                                foundType
                        ), (LivingEntity) foundType.create(level(), EntitySpawnReason.COMMAND));
                    }
                }
            }
        }
    }

    // getDimensions is final in LivingEntity in 26.1, handled by EntityMixin instead

    /**
     * When a player turns into an Aquatic identity, they lose breath outside water.
     *
     * @param ci mixin callback info
     */
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void tickAquaticBreathingOutsideWater(CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(identity != null) {
            if(Identity.isAquatic(identity)) {
                int air = this.getAirSupply();

                // copy of WaterAnimal#tickWaterBreathingAir
                if(this.isAlive() && !this.isInWater()) {
                    int i = EnchantmentHelper.getEnchantmentLevel(
                            level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.RESPIRATION),
                            (LivingEntity) (Object) this);

                    // If the player has respiration, 50% chance to not consume air
                    if(i > 0) {
                        if(random.nextInt(i + 1) <= 0) {
                            this.setAirSupply(air - 1);
                        }
                    }

                    // No respiration, decrease air as normal
                    else {
                        this.setAirSupply(air - 1);
                    }

                    // Air has ran out, start drowning
                    if(this.getAirSupply() == -20) {
                        this.setAirSupply(0);
                        this.hurt(damageSources().drown(), 2.0F);
                    }
                } else {
                    this.setAirSupply(300);
                }
            }
        }
    }

    @Inject(
            method = "getHurtSound",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(IdentityConfig.getInstance().useIdentitySounds() && identity != null) {
            cir.setReturnValue(((LivingEntityAccessor) identity).callGetHurtSound(source));
        }
    }


    // todo: separate mixin for ambient sounds
    private int identity_ambientSoundChance = 0;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void tickAmbientSounds(CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(!level().isClientSide() && IdentityConfig.getInstance().playAmbientSounds() && identity instanceof Mob) {
            Mob mobIdentity = (Mob) identity;

            if(this.isAlive() && this.random.nextInt(1000) < this.identity_ambientSoundChance++) {
                // reset sound delay
                this.identity_ambientSoundChance = -mobIdentity.getAmbientSoundInterval();

                // play ambient sound
                SoundEvent sound = ((MobEntityAccessor) mobIdentity).callGetAmbientSound();
                if(sound != null) {
                    float volume = ((LivingEntityAccessor) mobIdentity).callGetSoundVolume();
                    float pitch = ((LivingEntityAccessor) mobIdentity).callGetSoundPitch();

                    // By default, players can not hear their own ambient noises.
                    // This is because ambient noises can be very annoying.
                    if(IdentityConfig.getInstance().hearSelfAmbient()) {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), volume, pitch);
                    } else {
                        this.level().playSound((Player) (Object) this, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), volume, pitch);
                    }
                }
            }
        }
    }

    @Inject(
            method = "getDeathSound",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(IdentityConfig.getInstance().useIdentitySounds() && identity != null) {
            cir.setReturnValue(((LivingEntityAccessor) identity).callGetDeathSound());
        }
    }

    @Inject(
            method = "getFallSounds",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getFallSounds(CallbackInfoReturnable<LivingEntity.Fallsounds> cir) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(IdentityConfig.getInstance().useIdentitySounds() && identity != null) {
            cir.setReturnValue(identity.getFallSounds());
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    protected void identity_tryAttack(Entity target, CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(identity instanceof IronGolem golem) {
            ((IronGolemEntityAccessor) golem).setAttackTicksLeft(10);
        }

        if(identity instanceof Warden warden) {
            warden.attackAnimationState.start(tickCount);
        }

        if(identity instanceof Ravager ravager) {
            ((RavagerEntityAccessor) ravager).setAttackTick(10);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickGolemAttackTicks(CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(identity instanceof IronGolem golem) {
            IronGolemEntityAccessor accessor = (IronGolemEntityAccessor) golem;
            if(accessor.getAttackTicksLeft() > 0) {
                accessor.setAttackTicksLeft(accessor.getAttackTicksLeft() - 1);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickRavagerAttackTicks(CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(identity instanceof Ravager ravager) {
            RavagerEntityAccessor accessor = (RavagerEntityAccessor) ravager;
            if(accessor.getAttackTick() > 0) {
                accessor.setAttackTick(accessor.getAttackTick() - 1);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickWardenSneakingAnimation(CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(identity instanceof Warden warden) {
            if(isShiftKeyDown()) {
                if(!warden.sniffAnimationState.isStarted()) {
                    warden.sniffAnimationState.start(tickCount);
                }
            } else {
                warden.sniffAnimationState.stop();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickFire(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(player);

        if(!player.level().isClientSide() && !player.isCreative() && !player.isSpectator()) {
            if (identity != null) {
                EntityType<?> type = identity.getType();

                if (type.builtInRegistryHolder().is(IdentityEntityTags.BURNS_IN_DAYLIGHT)) {
                    boolean bl = this.isInDaylight();
                    if(bl) {

                        // Can't burn in the rain
                        if(player.level().isRaining()) {
                            return;
                        }

                        // check for helmets to negate burning
                        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
                        if(!itemStack.isEmpty()) {
                            if(itemStack.isDamageableItem()) {

                                // damage stack instead of burning player
                                itemStack.setDamageValue(itemStack.getDamageValue() + player.getRandom().nextInt(2));
                                if(itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                                    player.onEquippedItemBroken(itemStack.getItem(), EquipmentSlot.HEAD);
                                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                                }
                            }

                            bl = false;
                        }

                        // set player on fire
                        if(bl) {
                            player.igniteForSeconds(8);
                        }
                    }
                }
            }
        }
    }


    @Unique
    private boolean isInDaylight() {
        if(level().getSkyDarken() < 4 && !level().isClientSide()) {
            float brightnessAtEyes = getLightLevelDependentMagicValue();
            BlockPos daylightTestPosition = new BlockPos((int) getX(), (int) Math.round(getY()), (int) getZ());

            // move test position up one block for boats
            if(getVehicle() instanceof Boat) {
                daylightTestPosition = daylightTestPosition.above();
            }

            return brightnessAtEyes > 0.5F && random.nextFloat() * 30.0F < (brightnessAtEyes - 0.4F) * 2.0F && level().getMaxLocalRawBrightness(daylightTestPosition) > 0;
        }

        return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickTemperature(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(player);

        if(!player.isCreative() && !player.isSpectator()) {
            // check if the player is identity
            if(identity != null) {
                EntityType<?> type = identity.getType();

                // damage player if they are an identity that gets hurt by high temps (eg. snow golem in nether)
                if(type.builtInRegistryHolder().is(IdentityEntityTags.HURT_BY_HIGH_TEMPERATURE)) {
                    Biome biome = level().getBiome(blockPosition()).value();
                    if (!biome.coldEnoughToSnow(blockPosition(), level().getSeaLevel())) {
                        player.hurt(damageSources().onFire(), 1.0f);
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickIdentity(CallbackInfo ci) {
        if(!level().isClientSide()) {
            Player player = (Player) (Object) this;
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            // assign basic data to entity from player on server; most data transferring occurs on client
            if(identity != null) {
                identity.setPos(player.getX(), player.getY(), player.getZ());
                identity.setYHeadRot(player.getYHeadRot());
                identity.setJumping(((LivingEntityAccessor) player).isJumping());
                identity.setSprinting(player.isSprinting());
                identity.setArrowCount(player.getArrowCount());
                identity.setInvulnerable(true);
                identity.setNoGravity(true);
                identity.setShiftKeyDown(player.isShiftKeyDown());
                identity.setSwimming(player.isSwimming());
                identity.startUsingItem(player.getUsedItemHand());
                identity.setPose(player.getPose());

                if(identity instanceof TamableAnimal) {
                    ((TamableAnimal) identity).setInSittingPose(player.isShiftKeyDown());
                    ((TamableAnimal) identity).setOrderedToSit(player.isShiftKeyDown());
                }

                ((EntityAccessor) identity).identity_callSetFlag(7, player.isFallFlying());

                ((LivingEntityAccessor) identity).callTickActiveItemStack();
                PlayerIdentity.sync((ServerPlayer) player); // safe cast - context is server world
            }
        }
    }
}
