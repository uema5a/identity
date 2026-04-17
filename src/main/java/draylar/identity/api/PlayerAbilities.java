package draylar.identity.api;

import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.AbilitySyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public class PlayerAbilities {

    /**
     * Returns an integer representing the current ability cooldown of the specified {@link Player} in ticks.
     *
     * <p>
     * A return value of {@code 0} represents no cooldown, while 20 is 1 second.
     *
     * @param player player to retrieve ability cooldown for
     * @return cooldown, in ticks, of the specified player's ability
     */
    public static int getCooldown(Player player) {
        return ((PlayerDataProvider) player).getAbilityCooldown();
    }

    public static boolean canUseAbility(Player player) {
        return ((PlayerDataProvider) player).getAbilityCooldown() <= 0;
    }

    public static void setCooldown(Player player, int cooldown) {
        ((PlayerDataProvider) player).setAbilityCooldown(cooldown);
    }

    public static void sync(ServerPlayer player) {
        ServerPlayNetworking.send(player, new AbilitySyncPayload(((PlayerDataProvider) player).getAbilityCooldown()));
    }
}
