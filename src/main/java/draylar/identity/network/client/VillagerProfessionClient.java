package draylar.identity.network.client;

import draylar.identity.network.NetworkHandler.OpenProfessionScreenPayload;
import draylar.identity.screen.VillagerProfessionScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Client-side handler for the {@link OpenProfessionScreenPayload} packet.
 * The receiver is registered in {@link draylar.identity.network.ClientNetworking}.
 *
 * <p>Phase C TODO: {@link VillagerProfessionScreen} constructor still uses Yarn-mapped
 * {@code net.minecraft.util.math.BlockPos}; once the screen is ported to
 * {@code net.minecraft.core.BlockPos} the cast below can be removed.</p>
 */
@Environment(EnvType.CLIENT)
public class VillagerProfessionClient {

    public static void openScreen(OpenProfessionScreenPayload payload) {
        Identifier professionId = payload.professionId();
        BlockPos pos = payload.workstationPos();
        Identifier worldId = payload.worldId();
        String existingName = payload.hasExisting() ? payload.existingName() : null;
        String existingProfession = payload.hasExisting()
                ? (payload.existingProfessionId().isEmpty() ? null : payload.existingProfessionId())
                : null;

        // Phase C TODO: VillagerProfessionScreen needs porting from Yarn BlockPos to net.minecraft.core.BlockPos.
        // The cast below works at runtime (same bytecode class) but is not type-safe at source level.
        @SuppressWarnings("unchecked")
        net.minecraft.util.math.BlockPos yarnPos = (net.minecraft.util.math.BlockPos) (Object) pos;
        Minecraft.getInstance().setScreen(new VillagerProfessionScreen(professionId, yarnPos, worldId, existingName, existingProfession));
    }
}
