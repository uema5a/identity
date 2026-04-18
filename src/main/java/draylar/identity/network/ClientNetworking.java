package draylar.identity.network;

import draylar.identity.IdentityClient;
import draylar.identity.api.ApplicablePacket;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.config.IdentityConfig;
import draylar.identity.impl.DimensionsRefresher;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.AbilitySyncPayload;
import draylar.identity.network.NetworkHandler.ConfigSyncPayload;
import draylar.identity.network.NetworkHandler.FavoriteSyncPayload;
import draylar.identity.network.NetworkHandler.IdentitySyncPayload;
import draylar.identity.network.NetworkHandler.OpenProfessionScreenPayload;
import draylar.identity.network.NetworkHandler.UnlockSyncPayload;
import draylar.identity.network.NetworkHandler.UseAbilityPayload;
import draylar.identity.network.NetworkHandler.VillagerIdentitiesSyncPayload;
import draylar.identity.network.client.VillagerProfessionClient;
import draylar.identity.screen.IdentityScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClientNetworking implements NetworkHandler {

    public static void registerPacketHandlers() {
        // Identity sync: server tells client about a player's identity
        ClientPlayNetworking.registerGlobalReceiver(IdentitySyncPayload.TYPE, (payload, context) -> {
            final UUID uuid = payload.playerUuid();
            final String id = payload.entityTypeId();
            final CompoundTag entityNbt = payload.entityNbt();

            context.client().execute(() -> {
                runOrQueue(context.player(), player -> {
                    @Nullable Player syncTarget = player.level().getPlayerByUUID(uuid);

                    if (syncTarget != null) {
                        PlayerDataProvider data = (PlayerDataProvider) syncTarget;

                        // Set identity to null (no identity) if the entity id is "minecraft:empty"
                        if (id.equals("minecraft:empty")) {
                            data.setIdentity(null);
                            ((DimensionsRefresher) syncTarget).identity_refreshDimensions();
                            return;
                        }

                        // If entity type was valid, deserialize entity data from tag
                        if (entityNbt != null) {
                            entityNbt.putString("id", id);
                            Optional<EntityType<?>> type = EntityType.byString(id);
                            if (type.isPresent()) {
                                LivingEntity identity = data.getIdentity();

                                // Ensure entity data exists
                                if (identity == null || !type.get().equals(identity.getType())) {
                                    identity = (LivingEntity) type.get().create(syncTarget.level(), EntitySpawnReason.LOAD);
                                    data.setIdentity(identity);

                                    // Refresh player dimensions/hitbox on client
                                    ((DimensionsRefresher) syncTarget).identity_refreshDimensions();
                                }

                                if (identity != null) {
                                    // In MC 26.1, Entity.load() takes a ValueInput
                                    ValueInput input = TagValueInput.create(
                                            ProblemReporter.DISCARDING,
                                            syncTarget.level().registryAccess(),
                                            entityNbt
                                    );
                                    identity.load(input);
                                }
                            }
                        }
                    }
                });
            });
        });

        // Favorite sync: server sends all favorites to client
        ClientPlayNetworking.registerGlobalReceiver(FavoriteSyncPayload.TYPE, (payload, context) -> {
            CompoundTag tag = payload.data();

            context.client().execute(() -> {
                runOrQueue(context.player(), player -> {
                    PlayerDataProvider data = (PlayerDataProvider) player;
                    data.getFavorites().clear();
                    ListTag idList = tag.getListOrEmpty("FavoriteIdentities");
                    for (int i = 0; i < idList.size(); i++) {
                        idList.getCompound(i).ifPresent(compound -> {
                            IdentityType<?> type = IdentityType.from(compound);
                            if (type != null) {
                                data.getFavorites().add(type);
                            }
                        });
                    }
                });
            });
        });

        // Ability sync: server sends ability cooldown to client
        ClientPlayNetworking.registerGlobalReceiver(AbilitySyncPayload.TYPE, (payload, context) -> {
            int cooldown = payload.cooldown();
            context.client().execute(() -> {
                runOrQueue(context.player(), player -> ((PlayerDataProvider) player).setAbilityCooldown(cooldown));
            });
        });

        // Unlock sync: server sends all unlocked identity types to client
        ClientPlayNetworking.registerGlobalReceiver(UnlockSyncPayload.TYPE, (payload, context) -> {
            CompoundTag nbt = payload.data();

            context.client().execute(() -> {
                if (nbt != null) {
                    ListTag list = nbt.getListOrEmpty("UnlockedIdentities");
                    runOrQueue(context.player(), player -> {
                        ((PlayerDataProvider) player).getUnlocked().clear();
                        for (int i = 0; i < list.size(); i++) {
                            list.getCompound(i).ifPresent(compound -> {
                                IdentityType<?> type = IdentityType.from(compound);
                                if (type != null) {
                                    ((PlayerDataProvider) player).getUnlocked().add(type);
                                }
                            });
                        }
                    });
                }
            });
        });

        // Config sync: server sends config values to client
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
            boolean enableClientSwapMenu = payload.enableClientSwapMenu();
            boolean showPlayerNametag = payload.showPlayerNametag();

            context.client().execute(() -> {
                IdentityConfig.getInstance().enableClientSwapMenu = enableClientSwapMenu;
                IdentityConfig.getInstance().showPlayerNametag = showPlayerNametag;
            });
        });

        // xGabou villager S2C payloads
        // OpenProfessionScreen: server asks client to open profession-selection UI.
        ClientPlayNetworking.registerGlobalReceiver(OpenProfessionScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> VillagerProfessionClient.openScreen(payload));
        });

        // VillagerIdentitiesSync: server syncs villager identity map + active key.
        ClientPlayNetworking.registerGlobalReceiver(VillagerIdentitiesSyncPayload.TYPE, (payload, context) -> {
            CompoundTag root = payload.data();

            context.client().execute(() -> {
                runOrQueue(context.player(), player -> {
                    if (root == null) return;
                    PlayerDataProvider data = (PlayerDataProvider) player;
                    @SuppressWarnings("unchecked")
                    Map<String, CompoundTag> villagerIds =
                            (Map<String, CompoundTag>) (Map<?, ?>) data.getVillagerIdentities();
                    villagerIds.clear();
                    CompoundTag villagerTag = root.getCompound("VillagerIdentities");
                    for (String key : villagerTag.getAllKeys()) {
                        villagerTag.getCompound(key).ifPresent(tag -> villagerIds.put(key, tag));
                    }
                    String active = root.contains("ActiveVillagerKey", Tag.TAG_STRING)
                            ? root.getString("ActiveVillagerKey") : null;
                    data.setActiveVillagerKey(active == null || active.isEmpty() ? null : active);

                    // Refresh identity screen if open
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.screen instanceof IdentityScreen screen) {
                        screen.resize(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                    }
                });
            });
        });
    }

    public static void runOrQueue(Player player, ApplicablePacket packet) {
        if (player == null) {
            IdentityClient.getSyncPacketQueue().add(packet);
        } else {
            packet.apply(player);
        }
    }

    public static void sendAbilityRequest() {
        ClientPlayNetworking.send(new UseAbilityPayload());
    }

    private ClientNetworking() {
        // NO-OP
    }
}
