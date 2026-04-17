package draylar.identity.network.impl;

import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.UnlockSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

public class UnlockPackets {

    private static final String UNLOCK_KEY = "UnlockedIdentities";

    public static void sendSyncPacket(ServerPlayer player) {
        // Serialize unlocked to tag
        CompoundTag compound = new CompoundTag();
        ListTag idList = new ListTag();
        ((PlayerDataProvider) player).getUnlocked().forEach(type -> idList.add(type.writeCompound()));
        compound.put(UNLOCK_KEY, idList);

        // Send to client
        ServerPlayNetworking.send(player, new UnlockSyncPayload(compound));
    }
}
