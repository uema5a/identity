package draylar.identity.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface IdentitySwapCallback {
    Event<IdentitySwapCallback> EVENT = EventFactory.createArrayBacked(IdentitySwapCallback.class,
            listeners -> (player, to) -> {
                for (IdentitySwapCallback listener : listeners) {
                    InteractionResult result = listener.swap(player, to);
                    if (result != InteractionResult.PASS) return result;
                }
                return InteractionResult.PASS;
            });

    InteractionResult swap(ServerPlayer player, @Nullable LivingEntity to);
}
