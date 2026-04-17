package draylar.identity.api.event;

import draylar.identity.api.variant.IdentityType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface UnlockIdentityCallback {
    Event<UnlockIdentityCallback> EVENT = EventFactory.createArrayBacked(UnlockIdentityCallback.class,
            listeners -> (player, type) -> {
                for (UnlockIdentityCallback listener : listeners) {
                    InteractionResult result = listener.unlock(player, type);
                    if (result != InteractionResult.PASS) return result;
                }
                return InteractionResult.PASS;
            });

    InteractionResult unlock(ServerPlayer player, IdentityType type);
}
