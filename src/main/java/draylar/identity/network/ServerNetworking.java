package draylar.identity.network;

import draylar.identity.ability.AbilityRegistry;
import draylar.identity.api.PlayerAbilities;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.network.NetworkHandler.SaveProfessionPayload;
import draylar.identity.network.NetworkHandler.TradeSyncPayload;
import draylar.identity.network.NetworkHandler.UseAbilityPayload;
import draylar.identity.network.impl.FavoritePackets;
import draylar.identity.network.impl.SwapPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class ServerNetworking implements NetworkHandler {

    public static void initialize() {
        // Register all payload types (both C2S and S2C)
        NetworkHandler.registerPayloadTypes();

        // Register C2S handlers
        FavoritePackets.registerFavoriteRequestHandler();
        SwapPackets.registerIdentityRequestPacketHandler();

        // xGabou villager C2S handlers (stubs — Phase B8 ports them)
        ServerPlayNetworking.registerGlobalReceiver(SaveProfessionPayload.TYPE, (payload, context) -> {
            // TODO Phase B: port from old VillagerProfessionPackets#registerServerHandler
        });

        ServerPlayNetworking.registerGlobalReceiver(TradeSyncPayload.TYPE, (payload, context) -> {
            // TODO Phase B: port from old VillagerTradePackets#registerTradeRequestHandler
        });
    }

    public static void registerUseAbilityPacketHandler() {
        ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();

            context.server().execute(() -> {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // Verify we should use ability for the player's current identity
                if (identity != null) {
                    EntityType<?> identityType = identity.getType();

                    if (AbilityRegistry.has(identityType)) {
                        // Check cooldown
                        if (PlayerAbilities.canUseAbility(player)) {
                            AbilityRegistry.get(identityType).onUse(player, identity, player.level());
                            PlayerAbilities.setCooldown(player, AbilityRegistry.get(identityType).getCooldown(identity));
                            PlayerAbilities.sync(player);
                        }
                    }
                }
            });
        });
    }
}
