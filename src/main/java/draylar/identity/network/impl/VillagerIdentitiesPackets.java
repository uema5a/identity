package draylar.identity.network.impl;

import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.VillagerIdentitiesSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class VillagerIdentitiesPackets {

    public static void sendSync(ServerPlayer player) {
        PlayerDataProvider data = (PlayerDataProvider) player;

        CompoundTag root = new CompoundTag();
        CompoundTag villagerTag = new CompoundTag();
        @SuppressWarnings("unchecked")
        Map<String, CompoundTag> villagerIdentities = (Map<String, CompoundTag>) (Map<?, ?>) data.getVillagerIdentities();
        for (Map.Entry<String, CompoundTag> entry : villagerIdentities.entrySet()) {
            villagerTag.put(entry.getKey(), entry.getValue().copy());
        }
        root.put("VillagerIdentities", villagerTag);
        String active = data.getActiveVillagerKey();
        if (active != null && !active.isEmpty()) {
            root.putString("ActiveVillagerKey", active);
        }

        ServerPlayNetworking.send(player, new VillagerIdentitiesSyncPayload(root));
    }
}
