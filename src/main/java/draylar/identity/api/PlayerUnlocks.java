package draylar.identity.api;

import draylar.identity.api.event.UnlockIdentityCallback;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.impl.UnlockPackets;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class PlayerUnlocks {

    public static boolean unlock(ServerPlayer player, IdentityType granted) {
        PlayerDataProvider provider = (PlayerDataProvider) player;
        InteractionResult unlock = UnlockIdentityCallback.EVENT.invoker().unlock(player, granted);

        if(unlock != InteractionResult.FAIL && !provider.getUnlocked().contains(granted)) {
            provider.getUnlocked().add(granted);
            sync(player);
            PlayerAbilities.sync(player); // TODO: ???
            return true;
        } else {
            return false;
        }
    }

    public static boolean has(Player player, IdentityType type) {
        return type.getEntityType().equals(EntityType.PLAYER) || (((PlayerDataProvider) player)).getUnlocked().contains(type);
    }

    public static void revoke(ServerPlayer player, IdentityType type) {
        PlayerDataProvider provider = (PlayerDataProvider) player;

        if(provider.getUnlocked().contains(type)) {
            provider.getUnlocked().remove(type);
            sync(player);
            PlayerAbilities.sync(player); // TODO: ???
        }
    }

    public static void sync(ServerPlayer player) {
        UnlockPackets.sendSyncPacket(player);
    }
}
