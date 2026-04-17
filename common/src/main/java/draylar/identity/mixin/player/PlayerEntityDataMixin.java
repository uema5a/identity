package draylar.identity.mixin.player;

import net.minecraft.world.InteractionResult;
import draylar.identity.Identity;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.event.IdentitySwapCallback;
import draylar.identity.api.FlightHelper;
import draylar.identity.api.platform.IdentityConfig;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.DimensionsRefresher;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Player.class)
public abstract class PlayerEntityDataMixin extends LivingEntity implements PlayerDataProvider {

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);
    @Unique private static final String ABILITY_COOLDOWN_KEY = "AbilityCooldown";
    @Unique private final Set<IdentityType<?>> unlocked = new HashSet<>();
    @Unique private final Set<IdentityType<?>> favorites = new HashSet<>();
    @Unique private int remainingTime = 0;
    @Unique private int abilityCooldown = 0;
    @Unique private LivingEntity identity = null;
    @Unique private IdentityType<?> identityType = null;

    private PlayerEntityDataMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readNbt(ValueInput input, CallbackInfo info) {
        unlocked.clear();
        readIdentityTypeSet(input, "UnlockedIdentities", unlocked);

        favorites.clear();
        readIdentityTypeSet(input, "FavoriteIdentitiesV2", favorites);

        abilityCooldown = input.getIntOr(ABILITY_COOLDOWN_KEY, 0);
        remainingTime = input.getIntOr("RemainingHostilityTime", 0);

        input.child("CurrentIdentity").ifPresent(this::readCurrentIdentity);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(ValueOutput output, CallbackInfo info) {
        writeIdentityTypeSet(output, "UnlockedIdentities", unlocked);
        writeIdentityTypeSet(output, "FavoriteIdentitiesV2", favorites);

        output.putInt(ABILITY_COOLDOWN_KEY, abilityCooldown);
        output.putInt("RemainingHostilityTime", remainingTime);

        writeCurrentIdentity(output.child("CurrentIdentity"));
    }

    @Unique
    private static void readIdentityTypeSet(ValueInput input, String key, Set<IdentityType<?>> target) {
        for (ValueInput child : input.childrenListOrEmpty(key)) {
            String entityId = child.getStringOr("EntityID", "");
            if (!entityId.isEmpty()) {
                EntityType.byString(entityId).ifPresent(entityType -> {
                    IdentityType<?> type = IdentityType.from(entityType, child.getIntOr("Variant", -1));
                    if (type != null) {
                        target.add(type);
                    }
                });
            }
        }
    }

    @Unique
    private static void writeIdentityTypeSet(ValueOutput output, String key, Set<IdentityType<?>> source) {
        ValueOutput.ValueOutputList list = output.childrenList(key);
        for (IdentityType<?> type : source) {
            ValueOutput child = list.addChild();
            child.putString("EntityID", BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString());
            child.putInt("Variant", type.getVariantData());
        }
    }

    @Unique
    private void writeCurrentIdentity(ValueOutput output) {
        output.putString("id", identity == null ? "minecraft:empty" : BuiltInRegistries.ENTITY_TYPE.getKey(identity.getType()).toString());

        if (identity != null) {
            identity.saveWithoutId(output.child("EntityData"));
            if (identityType != null) {
                ValueOutput typeOutput = output.child("IdentityType");
                typeOutput.putString("EntityID", BuiltInRegistries.ENTITY_TYPE.getKey(identityType.getEntityType()).toString());
                typeOutput.putInt("Variant", identityType.getVariantData());
            }
        }
    }

    @Unique
    public void readCurrentIdentity(ValueInput input) {
        String idStr = input.getStringOr("id", "minecraft:empty");

        if (idStr.equals("minecraft:empty")) {
            this.identity = null;
            ((DimensionsRefresher) this).identity_refreshDimensions();
        } else {
            Optional<EntityType<?>> type = EntityType.byString(idStr);
            if (type.isPresent()) {
                if (identity == null || !type.get().equals(identity.getType())) {
                    identity = (LivingEntity) type.get().create(level(), EntitySpawnReason.LOAD);
                    ((DimensionsRefresher) this).identity_refreshDimensions();
                }

                Optional<ValueInput> typeInputOpt = input.child("IdentityType");
                if (typeInputOpt.isPresent()) {
                    ValueInput typeInput = typeInputOpt.get();
                    String typeEntityId = typeInput.getStringOr("EntityID", "");
                    if (!typeEntityId.isEmpty()) {
                        Optional<EntityType<?>> typeEntityType = EntityType.byString(typeEntityId);
                        if (typeEntityType.isPresent()) {
                            identityType = IdentityType.from(typeEntityType.get(), typeInput.getIntOr("Variant", -1));
                        }
                    }
                }
            }
        }
    }

    @Unique
    @Override
    public Set<IdentityType<?>> getUnlocked() {
        return unlocked;
    }

    @Override
    public void setUnlocked(Set<IdentityType<?>> unlocked) {
        this.unlocked.clear();
        this.unlocked.addAll(unlocked);
    }

    @Unique
    @Override
    public Set<IdentityType<?>> getFavorites() {
        return favorites;
    }

    @Override
    public void setFavorites(Set<IdentityType<?>> favorites) {
        this.favorites.clear();
        this.favorites.addAll(favorites);
    }

    @Unique
    @Override
    public int getRemainingHostilityTime() {
        return remainingTime;
    }

    @Unique
    @Override
    public void setRemainingHostilityTime(int max) {
        remainingTime = max;
    }

    @Unique
    @Override
    public int getAbilityCooldown() {
        return abilityCooldown;
    }

    @Unique
    @Override
    public void setAbilityCooldown(int abilityCooldown) {
        this.abilityCooldown = abilityCooldown;
    }

    @Unique
    @Override
    public LivingEntity getIdentity() {
        return identity;
    }

    @Override
    public IdentityType<?> getIdentityType() {
        return identityType;
    }

    @Unique
    @Override
    public void setIdentity(LivingEntity identity) {
        this.identity = identity;
    }

    // Phase C TODO: add setIdentityType to PlayerDataProvider interface (B10/Phase C)
    @Unique
    public void setIdentityType(@Nullable IdentityType<?> type) {
        this.identityType = type;
    }

    // Phase C TODO: change PlayerDataProvider.updateIdentity to (IdentityType<?>, LivingEntity) (B10/Phase C)
    @Override
    public boolean updateIdentity(@Nullable LivingEntity identity) {
        return updateIdentity(null, identity);
    }

    @Unique
    public boolean updateIdentity(@Nullable IdentityType<?> type, @Nullable LivingEntity identity) {
        Player player = (Player) (Object) this;
        InteractionResult result = IdentitySwapCallback.EVENT.invoker().swap((ServerPlayer) player, identity);
        if (result == InteractionResult.FAIL) {
            return false;
        }

        this.identity = identity;
        this.identityType = type;

        ((DimensionsRefresher) player).identity_refreshDimensions();

        if (identity != null && IdentityConfig.getInstance().scalingHealth()) {
            player.setHealth(Math.min(player.getHealth(), identity.getMaxHealth()));
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(IdentityConfig.getInstance().maxHealth(), identity.getMaxHealth()));
        }

        if (identity == null) {
            if (IdentityConfig.getInstance().scalingHealth()) {
                player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
            }
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (Identity.hasFlyingPermissions(serverPlayer)) {
            FlightHelper.grantFlightTo(serverPlayer);
            player.getAbilities().setFlyingSpeed(IdentityConfig.getInstance().flySpeed());
        } else {
            FlightHelper.revokeFlight(serverPlayer);
            player.getAbilities().setFlyingSpeed(0.05f);
        }
        player.onUpdateAbilities();

        // Dismount Ravager if the new identity cannot ride one
        if (player.getVehicle() instanceof Ravager && (identity == null || !identity.getType().builtInRegistryHolder().is(IdentityEntityTags.RAVAGER_RIDING))) {
            player.stopRiding();
        }

        if (!player.level().isClientSide()) {
            PlayerIdentity.sync(serverPlayer);
        }

        return true;
    }
}
