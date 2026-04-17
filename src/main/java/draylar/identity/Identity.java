package draylar.identity;

import draylar.identity.ability.AbilityRegistry;
import draylar.identity.api.IdentityTickHandlers;
import draylar.identity.api.PlayerFavorites;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.PlayerUnlocks;
import draylar.identity.api.SafeTagManager;
import draylar.identity.config.IdentityConfig;
import draylar.identity.network.NetworkHandler.ConfigSyncPayload;
import draylar.identity.network.ServerNetworking;
import draylar.identity.registry.IdentityCommands;
import draylar.identity.registry.IdentityEntityTags;
import draylar.identity.registry.IdentityEventHandlers;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Guardian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Identity {

    public static final String MODID = "identity";
    public static final Logger LOGGER = LoggerFactory.getLogger(Identity.class);

    public void initialize() {
        IdentityEntityTags.init();
        AbilityRegistry.init();
        IdentityEventHandlers.initialize();
        IdentityCommands.init();
        ServerNetworking.initialize();
        ServerNetworking.registerUseAbilityPacketHandler();
        registerJoinSyncPacket();
        IdentityTickHandlers.initialize();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SafeTagManager.loadAll(server);
        });
    }

    public static void registerJoinSyncPacket() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;

            // Send config sync packet to newly joined player
            ServerPlayNetworking.send(player, new ConfigSyncPayload(
                    IdentityConfig.getInstance().enableClientSwapMenu(),
                    IdentityConfig.getInstance().showPlayerNametag()
            ));

            // Sync unlocked Identity
            PlayerUnlocks.sync(player);

            // Sync favorites
            PlayerFavorites.sync(player);
            draylar.identity.network.impl.VillagerIdentitiesPackets.sendSync(player);
        });
    }

    public static Identifier id(String name) {
        return Identifier.parse("identity:" + name);
    }

    public static boolean hasFlyingPermissions(ServerPlayer player) {
        LivingEntity identity = PlayerIdentity.getIdentity(player);
        if (identity == null) {
            return false;
        }
        if (!IdentityConfig.getInstance().enableFlight() || !isAbleToFly(identity)) {
            return false;
        }

        List<String> requiredAdvancements = IdentityConfig.getInstance().advancementsRequiredForFlight();

        // requires at least 1 advancement, check if player has them
        if (requiredAdvancements != null && !requiredAdvancements.isEmpty()) {
            boolean hasPermission = true;
            for (String requiredAdvancement : requiredAdvancements) {
                AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Identifier.parse(requiredAdvancement));
                if (advancement == null) {
                    hasPermission = false;
                    break;
                }
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);

                if (!progress.isDone()) {
                    hasPermission = false;
                    break;
                }
            }
            return hasPermission;
        }

        return true;
    }

    private static boolean isAbleToFly(LivingEntity identity) {
        if (identity == null) return false;

        EntityType<?> type = identity.getType();
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String idString = id.toString();

        IdentityConfig config = IdentityConfig.getInstance();

        if (config.removedFlyingEntities().contains(idString)) return false;
        if (config.extraFlyingEntities().contains(idString)) return true;

        // Check both normal and custom flying tags
        return type.builtInRegistryHolder().is(IdentityEntityTags.FLYING) || SafeTagManager.isCustomFlying(type);
    }

    public static boolean isAquatic(LivingEntity entity) {
        return entity instanceof WaterAnimal || entity instanceof Guardian;
    }

    public static boolean identity$isAquatic(LivingEntity identity) {
        if (identity == null) {
            return false;
        }

        EntityType<?> type = identity.getType();
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String idString = id.toString();

        IdentityConfig config = IdentityConfig.getInstance();

        // REMOVE > ADD > TAG priority
        if (config.removedAquaticEntities().contains(idString)) {
            return false; // Player requested this mob NOT be aquatic
        }

        if (config.extraAquaticEntities().contains(idString)) {
            return true; // Player manually added it
        }

        // Otherwise, fallback to normal tag detection
        return type.builtInRegistryHolder().is(IdentityEntityTags.BREATHE_UNDERWATER) || SafeTagManager.isCustomBreatheUnderwater(type);
    }

    public static int getCooldown(EntityType<?> type) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        return IdentityConfig.getInstance().getAbilityCooldownMap().getOrDefault(id, 20);
    }
}
