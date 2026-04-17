package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import draylar.identity.impl.PlayerDataProvider;
import draylar.identity.network.impl.VillagerProfessionPackets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Holder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class VillagerProfessionAbility extends IdentityAbility<Villager> {

    @Override
    public void onUse(Player player, Villager identity, Level level) {
        if (level.isClientSide) {
            return;
        }

        if (!player.isShiftKeyDown()) {
            ((ServerPlayer) player).sendSystemMessage(Component.translatable("identity.profession.must_sneak"), true);
            return;
        }

        HitResult result = player.pick(5.0, 0.0F, false);
        if (result.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockResult = (BlockHitResult) result;
        // TODO Phase C: verify PoiTypes.forState method name in MC 26.1 (was PointOfInterestTypes.getTypeForState in 1.20.1)
        Optional<Holder<PoiType>> poi = PoiTypes.forState(level.getBlockState(blockResult.getBlockPos()));
        if (poi.isPresent()) {
            Holder<PoiType> targetPoi = poi.get();

            var poiKey = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(targetPoi.value());
            Identifier poiId = poiKey != null ? poiKey.location() : null;
            Identifier worldId = player.level().dimension().location();
            // TODO Phase C: PlayerDataProvider.getVillagerIdentities() will be migrated by B3 from NbtCompound to CompoundTag
            Map<String, CompoundTag> villagerMap = ((PlayerDataProvider) player).getVillagerIdentities();
            String existingName = null;
            String existingProfession = null;
            long workstationPos = blockResult.getBlockPos().asLong();

            for (Map.Entry<String, CompoundTag> entry : villagerMap.entrySet()) {
                CompoundTag saved = entry.getValue();
                if (matchesWorkstation(saved, worldId, workstationPos)) {
                    existingName = entry.getKey();
                    existingProfession = saved.getString("ProfessionId");
                    break;
                }
            }

            for (VillagerProfession profession : BuiltInRegistries.VILLAGER_PROFESSION) {
                boolean matches = false;

                // 1) Simple ID match: many mappings name POI types after the profession (e.g., minecraft:librarian)
                var profKey = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
                Identifier profIdDirect = profKey != null ? profKey.location() : null;
                if (poiId != null && poiId.equals(profIdDirect)) {
                    matches = true;
                }

                try {
                    Method held = VillagerProfession.class.getMethod("heldWorkstation");
                    Object value = held.invoke(profession);
                    if (!matches && value instanceof Holder<?> entry) {
                        matches = entry.equals(targetPoi);
                    }
                } catch (NoSuchMethodException e) {
                    try {
                        Method acquirable = VillagerProfession.class.getMethod("acquirableJobSite");
                        Object predicate = acquirable.invoke(profession);
                        if (!matches && predicate instanceof Predicate<?> raw) {
                            @SuppressWarnings("unchecked")
                            Predicate<Holder<PoiType>> p = (Predicate<Holder<PoiType>>) raw;
                            matches = p.test(targetPoi);
                        }
                    } catch (NoSuchMethodException ignored) {
                        try {
                            Method acquirable = VillagerProfession.class.getMethod("acquirableWorkstation");
                            Object predicate = acquirable.invoke(profession);
                            if (!matches && predicate instanceof Predicate<?> raw) {
                                @SuppressWarnings("unchecked")
                                Predicate<Holder<PoiType>> p = (Predicate<Holder<PoiType>>) raw;
                                matches = p.test(targetPoi);
                            }
                        } catch (NoSuchMethodException ignoredToo) {
                            // no-op; API mismatch we can't resolve here
                        } catch (IllegalAccessException | InvocationTargetException reflectError) {
                            // ignore and continue
                        }
                    } catch (IllegalAccessException | InvocationTargetException reflectError) {
                        // ignore and continue
                    }
                } catch (IllegalAccessException | InvocationTargetException reflectError) {
                    // ignore and continue
                }

                if (matches) {
                    // TODO Phase C: VillagerProfessionPackets.openScreen signature migrated by B8 worker (ServerPlayer, Identifier, BlockPos, Identifier, String, String)
                    VillagerProfessionPackets.openScreen((ServerPlayer) player, profIdDirect, blockResult.getBlockPos(), worldId, existingName, existingProfession);
                    break;
                }
            }
        } else {
            ((ServerPlayer) player).sendSystemMessage(Component.translatable("identity.profession.invalid_workstation"), true);
        }
    }

    @Override
    public Item getIcon() {
        return Items.EMERALD;
    }

    private boolean matchesWorkstation(CompoundTag tag, Identifier worldId, long workstationPos) {
        if (tag == null) {
            return false;
        }
        String dim = tag.getString("WorkstationDim");
        long storedPos = tag.contains("WorkstationPos") ? tag.getLong("WorkstationPos") : Long.MIN_VALUE;
        return !dim.isEmpty() && storedPos != Long.MIN_VALUE && worldId.toString().equals(dim) && storedPos == workstationPos;
    }
}
