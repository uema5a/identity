package draylar.identity.network.impl;

import draylar.identity.api.PlayerFavorites;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.FavoriteSyncPayload;
import draylar.identity.network.NetworkHandler.FavoriteUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FavoritePackets {

    public static void sendFavoriteRequest(IdentityType<?> type, boolean favorite) {
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString();
        ClientPlayNetworking.send(new FavoriteUpdatePayload(entityTypeId, type.getVariantData(), favorite));
    }

    public static void registerFavoriteRequestHandler() {
        ServerPlayNetworking.registerGlobalReceiver(FavoriteUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();

            context.server().execute(() -> {
                Identifier entityId = Identifier.parse(payload.entityTypeId());
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
                int variant = payload.variant();
                boolean favorite = payload.favorite();

                @Nullable IdentityType<?> type = IdentityType.from(entityType, variant);

                if (type != null) {
                    if (favorite) {
                        PlayerFavorites.favorite(player, type);
                    } else {
                        PlayerFavorites.unfavorite(player, type);
                    }
                }
            });
        });
    }

    public static void sendFavoriteSync(ServerPlayer player) {
        Set<IdentityType<?>> favorites = ((PlayerDataProvider) player).getFavorites();
        CompoundTag tag = new CompoundTag();
        ListTag idList = new ListTag();
        favorites.forEach(type -> idList.add(type.writeCompound()));
        tag.put("FavoriteIdentities", idList);

        ServerPlayNetworking.send(player, new FavoriteSyncPayload(tag));
    }
}
