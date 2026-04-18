package draylar.identity.network.impl;

import draylar.identity.Identity;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.config.IdentityConfig;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.IdentityRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SwapPackets {

    public static void registerIdentityRequestPacketHandler() {
        ServerPlayNetworking.registerGlobalReceiver(IdentityRequestPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            Identity.LOGGER.debug("[Identity] Received swap request: hasType={}, entityTypeId={}, variant={}", payload.hasType(), payload.entityTypeId(), payload.variant());

            context.server().execute(() -> {
                IdentityConfig config = IdentityConfig.getInstance();

                // Ensure player has permission to switch identities
                if (!config.enableSwaps() && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) &&
                        config.allowedSwappers().stream()
                                .noneMatch(p -> p.equalsIgnoreCase(player.getGameProfile().name()))) {
                    return;
                }

                if (payload.hasType()) {
                    Identifier entityId = Identifier.parse(payload.entityTypeId());
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
                    int variant = payload.variant();

                    Identity.LOGGER.debug("[Identity] Resolved entityType={}, enableSwaps={}", entityType, config.enableSwaps());

                    if (entityType.equals(EntityType.PLAYER)) {
                        PlayerIdentity.updateIdentity(player, null, null);
                        ((PlayerDataProvider) player).setActiveVillagerKey(null);
                    } else {
                        @Nullable IdentityType<LivingEntity> type = IdentityType.from(entityType, variant);
                        Identity.LOGGER.debug("[Identity] IdentityType.from() = {}", type);
                        if (type != null) {
                            try {
                                LivingEntity created;
                                String selectedVillagerKey = null;

                                if (entityType.equals(EntityType.VILLAGER) && variant >= 1_000_000) {
                                    int index = variant - 1_000_000;
                                    PlayerDataProvider data = (PlayerDataProvider) player;
                                    @SuppressWarnings("unchecked")
                                    Map<String, CompoundTag> villagerIdentities = (Map<String, CompoundTag>) (Map<?, ?>) data.getVillagerIdentities();
                                    List<String> keys = new ArrayList<>(villagerIdentities.keySet());
                                    Collections.sort(keys);

                                    if (index >= 0 && index < keys.size()) {
                                        String key = keys.get(index);
                                        CompoundTag tag = villagerIdentities.get(key);
                                        CompoundTag copy = tag.copy();
                                        copy.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VILLAGER).toString());

                                        LivingEntity entity = (LivingEntity) EntityType.VILLAGER.create(player.level(), EntitySpawnReason.LOAD);
                                        if (entity != null) {
                                            entity.load(TagValueInput.create(
                                                    ProblemReporter.DISCARDING,
                                                    player.level().registryAccess(),
                                                    copy
                                            ));
                                            created = entity;
                                        } else {
                                            created = type.create(player.level());
                                        }
                                        selectedVillagerKey = key;
                                    } else {
                                        created = type.create(player.level());
                                    }
                                } else {
                                    created = type.create(player.level());
                                }

                                // Apply baby flag if requested
                                if (payload.baby() && created instanceof AgeableMob ageable) {
                                    ageable.setBaby(true);
                                }

                                Identity.LOGGER.debug("[Identity] Created entity = {}", created);
                                PlayerIdentity.updateIdentity(player, type, created);
                                ((PlayerDataProvider) player).setActiveVillagerKey(selectedVillagerKey);
                            } catch (Exception e) {
                                Identity.LOGGER.warn("Failed to create identity {}", entityType, e);
                            }
                        } else {
                            Identity.LOGGER.warn("[Identity] IdentityType.from() returned null for entityType={}, variant={}", entityType, variant);
                        }
                    }

                    // Refresh player dimensions
                    player.refreshDimensions();
                } else {
                    PlayerIdentity.updateIdentity(player, null, null);
                    ((PlayerDataProvider) player).setActiveVillagerKey(null);
                    player.refreshDimensions();
                }
            });
        });
    }

    public static void sendSwapRequest(@Nullable IdentityType<?> type, boolean baby) {
        Identity.LOGGER.debug("[Identity] Client sending swap request: type={}, baby={}", type, baby);
        if (type != null) {
            String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString();
            Identity.LOGGER.debug("[Identity] Sending payload: entityTypeId={}, variant={}, baby={}", entityTypeId, type.getVariantData(), baby);
            ClientPlayNetworking.send(new IdentityRequestPayload(true, entityTypeId, type.getVariantData(), baby));
        } else {
            ClientPlayNetworking.send(new IdentityRequestPayload(false, "", 0, false));
        }
    }
}
