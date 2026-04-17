package draylar.identity;

import com.mojang.blaze3d.platform.InputConstants;
import draylar.identity.ability.AbilityOverlayRenderer;
import draylar.identity.api.ApplicablePacket;
import draylar.identity.api.model.EntityArms;
import draylar.identity.api.model.EntityUpdaters;
import draylar.identity.impl.join.ClientPlayerJoinHandler;
import draylar.identity.impl.tick.AbilityKeyPressHandler;
import draylar.identity.impl.tick.MenuKeyPressHandler;
import draylar.identity.network.ClientNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class IdentityClient {

    private static final KeyMapping.Category IDENTITY_CATEGORY = KeyMapping.Category.register(
            Identifier.parse("identity:identity"));

    public static final KeyMapping MENU_KEY =
            new KeyMapping(
                    "key.identity",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_GRAVE_ACCENT,
                    IDENTITY_CATEGORY);

    public static final KeyMapping ABILITY_KEY =
            new KeyMapping(
                    "key.identity_ability",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    IDENTITY_CATEGORY);

    private static final Set<ApplicablePacket> SYNC_PACKET_QUEUE = new HashSet<>();

    public void initialize() {
        KeyMappingHelper.registerKeyMapping(MENU_KEY);
        KeyMappingHelper.registerKeyMapping(ABILITY_KEY);

        // Register client-side event handlers
        EntityUpdaters.init();
        AbilityOverlayRenderer.register();
        EntityArms.init();

        // Register event handlers
        ClientTickEvents.START_CLIENT_TICK.register(new MenuKeyPressHandler());
        ClientTickEvents.START_CLIENT_TICK.register(new AbilityKeyPressHandler());
        ClientNetworking.registerPacketHandlers();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            new ClientPlayerJoinHandler().onPlayerJoin(client.player);
        });
    }

    // Queue mechanism for packets that arrive before Minecraft#player exists.
    public static Set<ApplicablePacket> getSyncPacketQueue() {
        return SYNC_PACKET_QUEUE;
    }
}
