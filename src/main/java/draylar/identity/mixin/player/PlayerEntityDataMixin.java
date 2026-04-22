package draylar.identity.mixin.player;

import net.minecraft.world.InteractionResult;
import draylar.identity.Identity;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.event.IdentitySwapCallback;
import draylar.identity.api.FlightHelper;
import draylar.identity.config.IdentityConfig;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.DimensionsRefresher;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
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
    @Unique private final Map<String, CompoundTag> villagerIdentities = new HashMap<>();
    @Unique private @Nullable String activeVillagerKey = null;

    private PlayerEntityDataMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readNbt(ValueInput input, CallbackInfo info) {
        unlocked.clear();

        // Read new-format Identity unlock information using ValueInput children list
        for (ValueInput child : input.childrenListOrEmpty("UnlockedIdentities")) {
            String entityId = child.getStringOr("EntityID", "");
            if (!entityId.isEmpty()) {
                CompoundTag compound = new CompoundTag();
                compound.putString("EntityID", entityId);
                compound.putInt("Variant", child.getIntOr("Variant", -1));
                IdentityType<?> type = IdentityType.from(compound);
                if (type != null) {
                    unlocked.add(type);
                }
            }
        }

        // Favorites
        favorites.clear();
        for (ValueInput child : input.childrenListOrEmpty("FavoriteIdentitiesV2")) {
            String entityId = child.getStringOr("EntityID", "");
            if (!entityId.isEmpty()) {
                CompoundTag compound = new CompoundTag();
                compound.putString("EntityID", entityId);
                compound.putInt("Variant", child.getIntOr("Variant", -1));
                IdentityType<?> type = IdentityType.from(compound);
                if (type != null) {
                    favorites.add(type);
                }
            }
        }

        // Abilities
        abilityCooldown = input.getIntOr(ABILITY_COOLDOWN_KEY, 0);

        // Hostility
        remainingTime = input.getIntOr("RemainingHostilityTime", 0);

        // Current Identity
        input.child("CurrentIdentity").ifPresent(this::readCurrentIdentity);

        // Align step height on load to avoid temporary desync
        if (identity != null) {
            // TODO Phase D: setStepHeight removed in MC 26.1; step height is now automatic via maxUpStep()
        } else {
            // TODO Phase D: setStepHeight removed in MC 26.1; step height is now automatic via maxUpStep()
        }

        // Villager Identities (xGabou extension) — stored as SNBT strings per key
        villagerIdentities.clear();
        for (ValueInput villagerChild : input.childrenListOrEmpty("VillagerIdentityEntries")) {
            String key = villagerChild.getStringOr("VillagerKey", "");
            String snbt = villagerChild.getStringOr("VillagerData", "");
            if (!key.isEmpty() && !snbt.isEmpty()) {
                try {
                    CompoundTag parsed = net.minecraft.nbt.TagParser.parseCompoundFully(snbt);
                    villagerIdentities.put(key, parsed);
                } catch (Exception e) {
                    // Skip malformed villager data
                }
            }
        }

        String storedKey = input.getStringOr("ActiveVillagerKey", "");
        activeVillagerKey = storedKey.isEmpty() ? null : storedKey;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(ValueOutput output, CallbackInfo info) {
        // Write 'Unlocked' Identity data
        {
            ValueOutput.ValueOutputList list = output.childrenList("UnlockedIdentities");
            for (IdentityType<?> type : unlocked) {
                CompoundTag compound = type.writeCompound();
                ValueOutput child = list.addChild();
                child.putString("EntityID", compound.getStringOr("EntityID", ""));
                child.putInt("Variant", compound.getIntOr("Variant", -1));
            }
        }

        // Favorites
        {
            ValueOutput.ValueOutputList list = output.childrenList("FavoriteIdentitiesV2");
            for (IdentityType<?> type : favorites) {
                CompoundTag compound = type.writeCompound();
                ValueOutput child = list.addChild();
                child.putString("EntityID", compound.getStringOr("EntityID", ""));
                child.putInt("Variant", compound.getIntOr("Variant", -1));
            }
        }

        // Abilities
        output.putInt(ABILITY_COOLDOWN_KEY, abilityCooldown);

        // Hostility
        output.putInt("RemainingHostilityTime", remainingTime);

        // Current Identity
        writeCurrentIdentity(output.child("CurrentIdentity"));

        // Villager Identities (xGabou extension) — each serialized as SNBT string
        ValueOutput.ValueOutputList villagerList = output.childrenList("VillagerIdentityEntries");
        villagerIdentities.forEach((key, compound) -> {
            ValueOutput entry = villagerList.addChild();
            entry.putString("VillagerKey", key);
            entry.putString("VillagerData", compound.toString());
        });

        if (activeVillagerKey != null && !activeVillagerKey.isEmpty()) {
            output.putString("ActiveVillagerKey", activeVillagerKey);
        }
    }

    @Unique
    private void writeCurrentIdentity(ValueOutput output) {
        // put entity type ID under the key "id", or "minecraft:empty" if no identity is equipped
        output.putString("id", identity == null
                ? "minecraft:empty"
                : BuiltInRegistries.ENTITY_TYPE.getKey(identity.getType()).toString());

        // serialize current identity data
        if (identity != null) {
            identity.saveWithoutId(output.child("EntityData"));
            if (identityType != null) {
                CompoundTag typeTag = identityType.writeCompound();
                ValueOutput typeOutput = output.child("IdentityType");
                typeOutput.putString("EntityID", typeTag.getStringOr("EntityID", ""));
                typeOutput.putInt("Variant", typeTag.getIntOr("Variant", -1));
            }
        }
    }

    @Unique
    public void readCurrentIdentity(ValueInput input) {
        String idStr = input.getStringOr("id", "minecraft:empty");

        // set identity to null (no identity) if the entity id is "minecraft:empty"
        if (idStr.equals("minecraft:empty")) {
            this.identity = null;
            ((DimensionsRefresher) this).identity_refreshDimensions();
        } else {
            // if entity type was valid, deserialize entity data
            Optional<EntityType<?>> type = EntityType.byString(idStr);
            if (type.isPresent()) {
                if (identity == null || !type.get().equals(identity.getType())) {
                    identity = (LivingEntity) type.get().create(level(), EntitySpawnReason.LOAD);
                    ((DimensionsRefresher) this).identity_refreshDimensions();
                }

                if (identity != null) {
                    input.child("EntityData").ifPresent(identity::load);
                }

                // Read identity type
                input.child("IdentityType").ifPresent(typeInput -> {
                    String entityId = typeInput.getStringOr("EntityID", "");
                    if (!entityId.isEmpty()) {
                        CompoundTag typeTag = new CompoundTag();
                        typeTag.putString("EntityID", entityId);
                        typeTag.putInt("Variant", typeInput.getIntOr("Variant", -1));
                        identityType = IdentityType.from(typeTag);
                    }
                });
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

    @Override
    public void setIdentityType(@Nullable IdentityType<?> type) {
        identityType = type;
    }

    @Override
    public Map<String, CompoundTag> getVillagerIdentities() {
        return villagerIdentities;
    }

    @Override
    public void setVillagerIdentity(String key, CompoundTag identity) {
        if (identity == null) {
            villagerIdentities.remove(key);
        } else {
            villagerIdentities.put(key, identity);
        }
    }

    @Override
    public void removeVillagerIdentity(String key) {
        villagerIdentities.remove(key);
        if (activeVillagerKey != null && activeVillagerKey.equals(key)) {
            activeVillagerKey = null;
        }
    }

    @Override
    public @Nullable String getActiveVillagerKey() {
        return activeVillagerKey;
    }

    @Override
    public void setActiveVillagerKey(@Nullable String key) {
        activeVillagerKey = key;
    }

    @Unique
    @Override
    public void setIdentity(LivingEntity identity) {
        this.identity = identity;
        if (!(identity instanceof Villager)) {
            activeVillagerKey = null;
        }
    }

    @Unique
    @Override
    public boolean updateIdentity(@Nullable IdentityType<?> type, @Nullable LivingEntity identity) {
        Player player = (Player) (Object) this;
        InteractionResult result = IdentitySwapCallback.EVENT.invoker().swap((ServerPlayer) player, identity);
        if (result == InteractionResult.FAIL) {
            return false;
        }

        this.identity = identity;
        if (!(identity instanceof Villager)) {
            activeVillagerKey = null;
        }

        // refresh entity hitbox dimensions
        ((DimensionsRefresher) player).identity_refreshDimensions();

        // Align server step height with identity (prevents movement desync)
        if (identity != null) {
            // TODO Phase D: setStepHeight removed in MC 26.1; step height is now automatic via maxUpStep()
        } else {
            // TODO Phase D: setStepHeight removed in MC 26.1; step height is now automatic via maxUpStep()
        }

        // Identity is valid and scaling health is on; set entity's max health and current health to reflect identity.
        if (identity != null && IdentityConfig.getInstance().scalingHealth()) {
            double oldMax = player.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            double newMax = Math.min(IdentityConfig.getInstance().maxHealth(), identity.getMaxHealth());
            identity$scaleHealth(player, oldMax, newMax);
        }

        // If the identity is null (going back to player), set the player's base health value to 20 (default) to clear old changes.
        if (identity == null && IdentityConfig.getInstance().scalingHealth()) {
            double oldMax = player.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            double newMax = 20.0;
            identity$scaleHealth(player, oldMax, newMax);
        }

        // update flight properties on player depending on identity
        ServerPlayer serverPlayerEntity = (ServerPlayer) player;
        boolean canFly = Identity.hasFlyingPermissions(serverPlayerEntity);
        Identity.LOGGER.info("[Identity] Swap flight update: identity={}, isBaby={}, canFly={}, wasMayfly={}",
                identity == null ? "null" : identity.getType().toString(),
                identity instanceof net.minecraft.world.entity.AgeableMob a ? a.isBaby() : "N/A",
                canFly, player.getAbilities().mayfly);
        if (canFly) {
            FlightHelper.grantFlightTo(serverPlayerEntity);
            player.getAbilities().setFlyingSpeed(IdentityConfig.getInstance().flySpeed());
            player.onUpdateAbilities();
        } else {
            FlightHelper.revokeFlight(serverPlayerEntity);
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }

        // If the player is riding a Ravager and changes into an Identity that cannot ride Ravagers, kick them off.
        if (player.getVehicle() instanceof Ravager) {
            if (identity == null || !identity.getType().builtInRegistryHolder().is(IdentityEntityTags.RAVAGER_RIDING)) {
                player.stopRiding();
            }
        }

        // sync with client
        if (!player.level().isClientSide()) {
            PlayerIdentity.sync((ServerPlayer) player);
        }

        return true;
    }

    @Unique
    private void identity$scaleHealth(Player player, double oldMax, double newMax) {
        double currentHealth = player.getHealth();
        double ratio = (oldMax > 0.0) ? (currentHealth / oldMax) : 1.0;
        double scaledHealth = net.minecraft.util.Mth.clamp(ratio * newMax, 1.0, newMax);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMax);
        player.setHealth((float) scaledHealth);
    }
}
