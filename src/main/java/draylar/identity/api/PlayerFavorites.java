package draylar.identity.api;

import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.impl.FavoritePackets;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class PlayerFavorites {

    public static boolean has(Player player, IdentityType type) {
        return type.getEntityType().equals(EntityType.PLAYER) || getFavorites(player).contains(type);
    }

    public static void favorite(ServerPlayer player, IdentityType type) {
        if(!getFavorites(player).contains(type)) {
            getFavorites(player).add(type);
            PlayerAbilities.sync(player);
        }

        sync(player);
    }

    public static void unfavorite(ServerPlayer player, IdentityType type) {
        if(getFavorites(player).contains(type)) {
            getFavorites(player).remove(type);
            PlayerAbilities.sync(player);
        }

        sync(player);
    }

    public static Set<IdentityType<?>> getFavorites(Player player) {
        return ((PlayerDataProvider) player).getFavorites();
    }

    public static void sync(ServerPlayer player) {
        FavoritePackets.sendFavoriteSync(player);
    }
}
