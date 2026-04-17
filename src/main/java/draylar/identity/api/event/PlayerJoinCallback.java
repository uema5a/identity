package draylar.identity.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerJoinCallback {
    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class,
            listeners -> (player) -> {
                for (PlayerJoinCallback listener : listeners) {
                    listener.onPlayerJoin(player);
                }
            });

    void onPlayerJoin(ServerPlayer player);
}
