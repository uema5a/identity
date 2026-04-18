package draylar.identity.network.impl;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.NetworkHandler.OpenProfessionScreenPayload;
import draylar.identity.network.NetworkHandler.SaveProfessionPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Map;

public class VillagerProfessionPackets {

    /**
     * Client-side: send a SaveProfession packet to the server.
     */
    @Environment(EnvType.CLIENT)
    public static void sendSetProfession(Identifier professionId, String name, boolean reset, BlockPos workstationPos, Identifier worldId, String originalName) {
        boolean hasOriginal = originalName != null;
        ClientPlayNetworking.send(new SaveProfessionPayload(
                professionId,
                name == null ? "" : name,
                reset,
                workstationPos,
                worldId,
                hasOriginal,
                hasOriginal ? originalName : ""
        ));
    }

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
        ServerLevel world = ((ServerLevel) player.level()).getServer().getLevel(worldKey);
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

        // Set profession using VillagerData.withProfession — looks up Holder from registry
        Holder<VillagerProfession> professionHolder = BuiltInRegistries.VILLAGER_PROFESSION
                .get(professionId)
                .map(ref -> (Holder<VillagerProfession>) ref)
                .orElse(BuiltInRegistries.VILLAGER_PROFESSION.get(VillagerProfession.NONE).orElse(null));

        if (professionHolder != null) {
            villager.setVillagerData(villager.getVillagerData().withProfession(professionHolder));
        }

        // Save villager entity data to CompoundTag via TagValueOutput
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
        villager.save(output);
        CompoundTag savedData = output.buildResult();
        // Copy all villager entity data fields we need
        tag.merge(savedData);

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
        String dim = tag.getStringOr("WorkstationDim", "");
        long storedPos = tag.contains("WorkstationPos") ? tag.getLongOr("WorkstationPos", Long.MIN_VALUE) : Long.MIN_VALUE;
        return !dim.isEmpty() && storedPos != Long.MIN_VALUE && worldId.toString().equals(dim) && storedPos == workstationPos;
    }
}
