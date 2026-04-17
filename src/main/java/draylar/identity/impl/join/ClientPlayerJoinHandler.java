package draylar.identity.impl.join;

import draylar.identity.IdentityClient;
import draylar.identity.api.ApplicablePacket;
import net.minecraft.client.player.LocalPlayer;

public class ClientPlayerJoinHandler {

    public void onPlayerJoin(LocalPlayer player) {
        if(player == null) return;

        for (ApplicablePacket packet : IdentityClient.getSyncPacketQueue()) {
            packet.apply(player);
        }

        IdentityClient.getSyncPacketQueue().clear();
    }
}
