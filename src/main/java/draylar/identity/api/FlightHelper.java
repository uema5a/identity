package draylar.identity.api;

import net.minecraft.server.level.ServerPlayer;

public class FlightHelper {

    public static void grantFlightTo(ServerPlayer player) {
        player.getAbilities().mayfly = true;
    }

    public static boolean hasFlight(ServerPlayer player) {
        return player.getAbilities().mayfly;
    }

    public static void revokeFlight(ServerPlayer player) {
        // Do not interfere with creative or spectator flight
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }
}
