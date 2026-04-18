package draylar.identity.network;

import draylar.identity.Identity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public interface NetworkHandler {

    // --- Payload Type Definitions ---

    /**
     * C2S: Client requests an identity swap.
     * Contains: boolean hasType, String entityTypeId (if hasType), int variant (if hasType)
     */
    record IdentityRequestPayload(boolean hasType, String entityTypeId, int variant, boolean baby) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<IdentityRequestPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, IdentityRequestPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeBoolean(payload.hasType);
                            if (payload.hasType) {
                                buf.writeUtf(payload.entityTypeId);
                                buf.writeInt(payload.variant);
                                buf.writeBoolean(payload.baby);
                            }
                        },
                        buf -> {
                            boolean hasType = buf.readBoolean();
                            if (hasType) {
                                return new IdentityRequestPayload(true, buf.readUtf(), buf.readInt(), buf.readBoolean());
                            }
                            return new IdentityRequestPayload(false, "", 0, false);
                        }
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * C2S: Client requests a favorite toggle.
     * Contains: String entityTypeId, int variant, boolean favorite
     */
    record FavoriteUpdatePayload(String entityTypeId, int variant, boolean favorite) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<FavoriteUpdatePayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("favorite"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FavoriteUpdatePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, FavoriteUpdatePayload::entityTypeId,
                        ByteBufCodecs.INT, FavoriteUpdatePayload::variant,
                        ByteBufCodecs.BOOL, FavoriteUpdatePayload::favorite,
                        FavoriteUpdatePayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * C2S: Client requests to use an ability.
     */
    record UseAbilityPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UseAbilityPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("use_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> STREAM_CODEC =
                StreamCodec.unit(new UseAbilityPayload());

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * S2C: Server syncs a player's identity to the client.
     * Contains: UUID playerUuid, String entityTypeId, CompoundTag entityNbt
     */
    record IdentitySyncPayload(UUID playerUuid, String entityTypeId, CompoundTag entityNbt) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<IdentitySyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("identity_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, IdentitySyncPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeUUID(payload.playerUuid);
                            buf.writeUtf(payload.entityTypeId);
                            buf.writeNbt(payload.entityNbt);
                        },
                        buf -> new IdentitySyncPayload(
                                buf.readUUID(),
                                buf.readUtf(),
                                buf.readNbt()
                        )
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * S2C: Server syncs favorites to the client.
     * Contains: CompoundTag with FavoriteIdentities list
     */
    record FavoriteSyncPayload(CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<FavoriteSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("favorite_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FavoriteSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.COMPOUND_TAG, FavoriteSyncPayload::data,
                        FavoriteSyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * S2C: Server syncs ability cooldown to the client.
     * Contains: int cooldown
     */
    record AbilitySyncPayload(int cooldown) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AbilitySyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("ability_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AbilitySyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, AbilitySyncPayload::cooldown,
                        AbilitySyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * S2C: Server syncs unlocked identity types to the client.
     * Contains: CompoundTag with UnlockedIdentities list
     */
    record UnlockSyncPayload(CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UnlockSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("unlock_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UnlockSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.COMPOUND_TAG, UnlockSyncPayload::data,
                        UnlockSyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * S2C: Server syncs config to the client.
     * Contains: boolean enableClientSwapMenu, boolean showPlayerNametag
     */
    record ConfigSyncPayload(boolean enableClientSwapMenu, boolean showPlayerNametag) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ConfigSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("config_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL, ConfigSyncPayload::enableClientSwapMenu,
                        ByteBufCodecs.BOOL, ConfigSyncPayload::showPlayerNametag,
                        ConfigSyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // --- xGabou villager extensions ---

    /**
     * S2C: Server asks the client to open the villager profession screen.
     * Schema derived from {@code common/.../VillagerProfessionPackets#openScreen}:
     *   Identifier professionId
     *   BlockPos  workstationPos
     *   Identifier worldId
     *   boolean   hasExisting
     *     if hasExisting:
     *       String  existingName
     *       String  existingProfessionId   (may be "" to represent null)
     */
    record OpenProfessionScreenPayload(
            Identifier professionId,
            BlockPos workstationPos,
            Identifier worldId,
            boolean hasExisting,
            String existingName,
            String existingProfessionId
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenProfessionScreenPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("open_profession_screen"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenProfessionScreenPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeIdentifier(payload.professionId);
                            buf.writeBlockPos(payload.workstationPos);
                            buf.writeIdentifier(payload.worldId);
                            buf.writeBoolean(payload.hasExisting);
                            if (payload.hasExisting) {
                                buf.writeUtf(payload.existingName == null ? "" : payload.existingName);
                                buf.writeUtf(payload.existingProfessionId == null ? "" : payload.existingProfessionId);
                            }
                        },
                        buf -> {
                            Identifier professionId = buf.readIdentifier();
                            BlockPos pos = buf.readBlockPos();
                            Identifier worldId = buf.readIdentifier();
                            boolean hasExisting = buf.readBoolean();
                            String existingName = "";
                            String existingProfessionId = "";
                            if (hasExisting) {
                                existingName = buf.readUtf();
                                existingProfessionId = buf.readUtf();
                            }
                            return new OpenProfessionScreenPayload(
                                    professionId, pos, worldId, hasExisting, existingName, existingProfessionId);
                        }
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * C2S: Client submits a profession/name change for a villager identity.
     * Schema derived from {@code common/.../VillagerProfessionPackets#sendSetProfession}:
     *   Identifier professionId
     *   String     name
     *   boolean    reset
     *   BlockPos   workstationPos
     *   Identifier worldId
     *   boolean    hasOriginal
     *     if hasOriginal:
     *       String originalName
     */
    record SaveProfessionPayload(
            Identifier professionId,
            String name,
            boolean reset,
            BlockPos workstationPos,
            Identifier worldId,
            boolean hasOriginal,
            String originalName
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SaveProfessionPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("set_profession"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SaveProfessionPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeIdentifier(payload.professionId);
                            buf.writeUtf(payload.name == null ? "" : payload.name);
                            buf.writeBoolean(payload.reset);
                            buf.writeBlockPos(payload.workstationPos);
                            buf.writeIdentifier(payload.worldId);
                            buf.writeBoolean(payload.hasOriginal);
                            if (payload.hasOriginal) {
                                buf.writeUtf(payload.originalName == null ? "" : payload.originalName);
                            }
                        },
                        buf -> {
                            Identifier professionId = buf.readIdentifier();
                            String name = buf.readUtf();
                            boolean reset = buf.readBoolean();
                            BlockPos pos = buf.readBlockPos();
                            Identifier worldId = buf.readIdentifier();
                            boolean hasOriginal = buf.readBoolean();
                            String originalName = "";
                            if (hasOriginal) {
                                originalName = buf.readUtf();
                            }
                            return new SaveProfessionPayload(
                                    professionId, name, reset, pos, worldId, hasOriginal, originalName);
                        }
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * C2S: Client requests to trade with the villager identity of another player (self or other).
     * Schema derived from {@code common/.../VillagerTradePackets#sendTradeRequest}:
     *   UUID target
     */
    record TradeSyncPayload(UUID target) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TradeSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("start_trade"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TradeSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), TradeSyncPayload::target,
                        TradeSyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * S2C: Server syncs villager identity list + active key to the client.
     * Schema derived from {@code common/.../VillagerIdentitiesPackets#sendSync}:
     *   CompoundTag root {
     *       CompoundTag VillagerIdentities { key -> CompoundTag }
     *       String      ActiveVillagerKey  (optional; empty string ~ null)
     *   }
     */
    record VillagerIdentitiesSyncPayload(CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<VillagerIdentitiesSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identity.id("villager_identities_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, VillagerIdentitiesSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.COMPOUND_TAG, VillagerIdentitiesSyncPayload::data,
                        VillagerIdentitiesSyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // --- Registration ---

    static void registerPayloadTypes() {
        // C2S payload types
        PayloadTypeRegistry.serverboundPlay().register(IdentityRequestPayload.TYPE, IdentityRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FavoriteUpdatePayload.TYPE, FavoriteUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SaveProfessionPayload.TYPE, SaveProfessionPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TradeSyncPayload.TYPE, TradeSyncPayload.STREAM_CODEC);

        // S2C payload types
        PayloadTypeRegistry.clientboundPlay().register(IdentitySyncPayload.TYPE, IdentitySyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FavoriteSyncPayload.TYPE, FavoriteSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AbilitySyncPayload.TYPE, AbilitySyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UnlockSyncPayload.TYPE, UnlockSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenProfessionScreenPayload.TYPE, OpenProfessionScreenPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(VillagerIdentitiesSyncPayload.TYPE, VillagerIdentitiesSyncPayload.STREAM_CODEC);
    }
}
