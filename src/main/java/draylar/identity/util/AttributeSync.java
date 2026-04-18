package draylar.identity.util;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;

public class AttributeSync {
    public static void syncMaxHealth(ServerPlayer player) {
        if (player == null) return;

        double max = player.getAttributeValue(Attributes.MAX_HEALTH);
        player.setHealth(Math.min(player.getHealth(), (float) max));
        // In MC 26.1 attribute changes are synced automatically by the server
    }
}
