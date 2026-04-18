package draylar.identity.network.impl;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.network.NetworkHandler.TradeSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.UUID;

public class VillagerTradePackets {

    public static void sendTradeRequest(UUID target) {
        ClientPlayNetworking.send(new TradeSyncPayload(target));
    }

    public static void registerTradeRequestHandler() {
        ServerPlayNetworking.registerGlobalReceiver(TradeSyncPayload.TYPE, (payload, context) -> {
            UUID targetId = payload.target();
            ServerPlayer requester = context.player();
            context.server().execute(() -> {
                ServerPlayer target = context.server().getPlayerList().getPlayer(targetId);
                if (target != null) {
                    LivingEntity identity = PlayerIdentity.getIdentity(target);
                    if (identity instanceof Villager villager) {
                        // Block self-trading unless enabled
                        if (requester.getUUID().equals(target.getUUID()) && !IdentityConfig.getInstance().allowSelfTrading()) {
                            return;
                        }
                        // Interact with the villager identity to open the trade screen
                        villager.mobInteract(requester, InteractionHand.MAIN_HAND);
                    }
                }
            });
        });
    }
}
