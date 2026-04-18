package draylar.identity.profession;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.impl.VillagerIdentitiesPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

import java.util.Iterator;
import java.util.Map;

public final class ProfessionLifecycle {

    private ProfessionLifecycle() {}

    public static void tickValidate(ServerPlayer player, int tickCount) {
        // run every 40 ticks to reduce load
        if ((tickCount % 40) != 0) return;

        Map<String, CompoundTag> map = ((PlayerDataProvider) player).getVillagerIdentities();
        Iterator<Map.Entry<String, CompoundTag>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CompoundTag> e = it.next();
            CompoundTag tag = e.getValue();
            if (tag == null) {
                continue;
            }

            String prof = tag.getString("ProfessionId");
            if (prof == null || prof.isEmpty()) continue; // unemployed

            String dim = tag.getString("WorkstationDim");
            long posLong = tag.contains("WorkstationPos") ? tag.getLong("WorkstationPos") : Long.MIN_VALUE;
            if (dim == null || dim.isEmpty() || posLong == Long.MIN_VALUE) {
                removeAndNotify(player, it, e.getKey(), prof);
                continue;
            }

            ServerLevel world = player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dim)));
            if (world == null) {
                removeAndNotify(player, it, e.getKey(), prof);
                continue;
            }

            BlockPos pos = BlockPos.of(posLong);
            if (world.isAir(pos)) {
                removeAndNotify(player, it, e.getKey(), prof);
                continue;
            }

            // Verify POI still exists at the workstation position
            if (PoiTypes.forState(world.getBlockState(pos)).isEmpty()) {
                removeAndNotify(player, it, e.getKey(), prof);
            }
        }
    }

    private static void removeAndNotify(ServerPlayer player, Iterator<Map.Entry<String, CompoundTag>> iterator, String key, String prof) {
        iterator.remove();
        PlayerDataProvider data = (PlayerDataProvider) player;
        if (key.equals(data.getActiveVillagerKey())) {
            data.setActiveVillagerKey(null);
        }

        player.sendSystemMessage(Component.translatable("identity.profession.block_destroyed", key, Component.literal(prof)));
        PlayerIdentity.sync(player);
        VillagerIdentitiesPackets.sendSync(player);
    }
}
