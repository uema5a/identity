package draylar.identity.network.impl;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.OpenProfessionScreenPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Map;

public class VillagerProfessionPackets {

    public static void openScreen(ServerPlayer player, Identifier professionId, BlockPos pos, Identifier worldId, String existingName, String existingProfessionId) {
        boolean hasExisting = existingName != null;
        ServerPlayNetworking.send(player, new OpenProfessionScreenPayload(
                professionId,
                pos,
                worldId,
                hasExisting,
                existingName == null ? "" : existingName,
                existingProfessionId == null ? "" : existingProfessionId
        ));
    }

    public static void handleServerRequest(ServerPlayer player, Identifier professionId, String rawName, boolean reset, BlockPos pos, Identifier worldId, String originalName) {
        PlayerDataProvider data = (PlayerDataProvider) player;
        @SuppressWarnings("unchecked")
        Map<String, CompoundTag> map = (Map<String, CompoundTag>) (Map<?, ?>) data.getVillagerIdentities();
        long workstationPos = pos.asLong();
        String trimmedName = rawName.trim();

        String existingKey = null;
        if (originalName != null && map.containsKey(originalName) && matchesWorkstation(map.get(originalName), worldId, workstationPos)) {
            existingKey = originalName;
        }
        if (existingKey == null) {
            for (Map.Entry<String, CompoundTag> entry : map.entrySet()) {
                if (matchesWorkstation(entry.getValue(), worldId, workstationPos)) {
                    existingKey = entry.getKey();
                    break;
                }
            }
        }

        if (reset) {
            if (existingKey != null) {
                data.removeVillagerIdentity(existingKey);
                player.sendSystemMessage(Component.translatable("identity.profession.removed", existingKey));
                PlayerIdentity.sync(player);
            } else {
                player.sendSystemMessage(Component.translatable("identity.profession.none"));
            }
            return;
        }

        if (trimmedName.isEmpty()) {
            player.sendSystemMessage(Component.translatable("identity.profession.require_name"));
            return;
        }

        if (map.containsKey(trimmedName) && (existingKey == null || !existingKey.equals(trimmedName))) {
            player.sendSystemMessage(Component.translatable("identity.profession.name_conflict", trimmedName));
            return;
        }

        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, worldId);
        ServerLevel world = player.server.getLevel(worldKey);
        if (world == null || !world.dimension().equals(player.level().dimension())) {
            player.sendSystemMessage(Component.translatable("identity.profession.invalid_world"));
            return;
        }

        if (PoiTypes.forState(world.getBlockState(pos)).isEmpty()) {
            player.sendSystemMessage(Component.translatable("identity.profession.invalid_workstation"));
            return;
        }

        if (!(PlayerIdentity.getIdentity(player) instanceof Villager villager)) {
            player.sendSystemMessage(Component.translatable("identity.profession.missing_identity"));
            return;
        }

        String professionIdStr = professionId.toString();
        CompoundTag tag = new CompoundTag();
        VillagerProfession profession =
                BuiltInRegistries.VILLAGER_PROFESSION.getOptional(professionId)
                        .orElse(VillagerProfession.NONE);

        villager.setVillagerData(new VillagerData(villager.getVillagerData().getType(), profession, villager.getVillagerData().getLevel()));
        villager.save(tag);
        tag.putString("ProfessionId", professionIdStr);
        tag.putString("WorkstationDim", worldId.toString());
        tag.putLong("WorkstationPos", workstationPos);
        tag.putString("IdentityName", trimmedName);

        data.setVillagerIdentity(trimmedName, tag);
        if (existingKey != null && !existingKey.equals(trimmedName)) {
            data.removeVillagerIdentity(existingKey);
        }

        String activeKey = data.getActiveVillagerKey();
        if (existingKey != null && existingKey.equals(activeKey)) {
            data.setActiveVillagerKey(trimmedName);
        } else if (existingKey == null) {
            data.setActiveVillagerKey(trimmedName);
        }

        Component professionText = Component.literal(professionIdStr);
        player.sendSystemMessage(Component.translatable(existingKey != null ? "identity.profession.updated" : "identity.profession.saved", trimmedName, professionText));
        world.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
        PlayerIdentity.sync(player);
        VillagerIdentitiesPackets.sendSync(player);
    }

    private static boolean matchesWorkstation(CompoundTag tag, Identifier worldId, long workstationPos) {
        if (tag == null) {
            return false;
        }
        String dim = tag.getString("WorkstationDim");
        long storedPos = tag.contains("WorkstationPos") ? tag.getLong("WorkstationPos") : Long.MIN_VALUE;
        return !dim.isEmpty() && storedPos != Long.MIN_VALUE && worldId.toString().equals(dim) && storedPos == workstationPos;
    }
}
