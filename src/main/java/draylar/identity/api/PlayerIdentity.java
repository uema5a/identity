package draylar.identity.api;

import draylar.identity.Identity;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.IdentitySyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.Map;

public class PlayerIdentity {

    /**
     * Returns the identity associated with the {@link Player} this component is attached to.
     *
     * <p>Note that this method may return null, which represents "no identity."
     *
     * @return the current {@link LivingEntity} identity associated with this component's player owner, or null if they have no identity equipped
     */
    public static LivingEntity getIdentity(Player player) {
        return ((PlayerDataProvider) player).getIdentity();
    }

    public static IdentityType<?> getIdentityType(Player player) {
        return ((PlayerDataProvider) player).getIdentityType();
    }

    // xGabou: villager identity tracking
    public static Map<String, CompoundTag> getVillagerIdentities(Player player) {
        return ((PlayerDataProvider) player).getVillagerIdentities();
    }

    public static void setVillagerIdentity(Player player, String key, CompoundTag identity) {
        ((PlayerDataProvider) player).setVillagerIdentity(key, identity);
    }

    public static void removeVillagerIdentity(Player player, String key) {
        ((PlayerDataProvider) player).removeVillagerIdentity(key);
    }

    /**
     * Sets the identity of the specified player.
     *
     * <p>Setting a identity refreshes the player's dimensions/hitbox, and toggles flight capabilities depending on the entity.
     * To clear this component's identity, pass null.
     *
     * @param entity {@link LivingEntity} new identity for this component, or null to clear
     */
    public static boolean updateIdentity(ServerPlayer player, IdentityType<?> type, LivingEntity entity) {
        // Protect against broken dragons from DragonMounts with null breed
        if(entity == null) {
            ((PlayerDataProvider) player).setIdentityType(type);
            return ((PlayerDataProvider) player).updateIdentity(type, null);
        }

        if(entity.getClass().getName().equals("com.github.kay9.dragonmounts.dragon.TameableDragon")) {
            try {
                Method getBreed = entity.getClass().getMethod("getBreed");
                Object breed = getBreed.invoke(entity);
                if(breed == null) {
                    player.sendSystemMessage(Component.literal("This dragon identity is broken (no breed). Identity not applied."));
                    return false;
                }
            } catch (Throwable t) {
                Identity.LOGGER.warn("[Identity] Failed to validate DragonMounts dragon breed", t);
                return false;
            }
        }

        ((PlayerDataProvider) player).setIdentityType(type);
        return ((PlayerDataProvider) player).updateIdentity(type, entity);
    }

    public static void sync(ServerPlayer player) {
        sync(player, player);
    }

    public static void sync(ServerPlayer changed, ServerPlayer packetTarget) {
        // Serialize current identity data to tag if it exists
        LivingEntity identity = getIdentity(changed);
        CompoundTag entityTag;
        if (identity != null) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, changed.level().registryAccess());
            identity.saveWithoutId(output);
            entityTag = output.buildResult();
        } else {
            entityTag = new CompoundTag();
        }

        // Put entity type ID, or "minecraft:empty" if no identity is equipped
        String entityTypeId = identity == null
                ? "minecraft:empty"
                : BuiltInRegistries.ENTITY_TYPE.getKey(identity.getType()).toString();

        ServerPlayNetworking.send(packetTarget, new IdentitySyncPayload(
                changed.getUUID(),
                entityTypeId,
                entityTag
        ));
    }
}
