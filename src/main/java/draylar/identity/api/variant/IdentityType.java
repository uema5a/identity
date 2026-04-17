package draylar.identity.api.variant;

import draylar.identity.Identity;
import draylar.identity.impl.variant.*;
import net.Gabou.gaboulibs.util.CompatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IdentityType<T extends LivingEntity> {

    private static final List<EntityType<? extends LivingEntity>> LIVING_TYPE_CASH = new ArrayList<>();
    private static final Map<EntityType<? extends LivingEntity>, TypeProvider<?>> VARIANT_BY_TYPE = new LinkedHashMap<>();
    private final EntityType<T> type;
    private final int variantData;

    static {
        VARIANT_BY_TYPE.put(EntityType.SHEEP, new SheepTypeProvider());
        VARIANT_BY_TYPE.put(EntityType.AXOLOTL, new AxolotlTypeProvider());
        VARIANT_BY_TYPE.put(EntityType.PARROT, new ParrotTypeProvider());
        VARIANT_BY_TYPE.put(EntityType.FOX, new FoxTypeProvider());
        VARIANT_BY_TYPE.put(EntityType.CAT, new CatTypeProvider());
        VARIANT_BY_TYPE.put(EntityType.SLIME, new SlimeTypeProvider());
        VARIANT_BY_TYPE.put(EntityType.FROG, new FrogTypeProvider());
    }

    public IdentityType(EntityType<T> type) {
        this.type = type;
        variantData = getDefaultVariantData(type);
    }

    private int getDefaultVariantData(EntityType<T> type) {
        if(VARIANT_BY_TYPE.containsKey(type)) {
            return VARIANT_BY_TYPE.get(type).getFallbackData();
        } else {
            return -1;
        }
    }

    public IdentityType(EntityType<T> type, int variantData) {
        this.type = type;
        this.variantData = variantData;
    }

    public IdentityType(T entity) {
        this.type = (EntityType<T>) entity.getType();

        // Discover variant data based on entity NBT data.
        @Nullable TypeProvider<T> provider = (TypeProvider<T>) VARIANT_BY_TYPE.get(type);
        if(provider != null) {
            variantData = provider.getVariantData(entity);
        } else {
            variantData = getDefaultVariantData(type);
        }
    }

    @Nullable
    public static <Z extends LivingEntity> IdentityType<Z> from(Z entity) {
        if(entity == null) {
            return null;
        }

        EntityType<Z> type = (EntityType<Z>) entity.getType();
        if(VARIANT_BY_TYPE.containsKey(type)) {
            TypeProvider<Z> typeProvider = (TypeProvider<Z>) VARIANT_BY_TYPE.get(type);
            return typeProvider.create(type, entity);
        }

        return new IdentityType<>((EntityType<Z>) entity.getType());
    }

    @Nullable
    public static IdentityType<?> from(CompoundTag compound) {
        String entityId = compound.getStringOr("EntityID", "");
        if(entityId.isEmpty()) return null;
        Identifier id = Identifier.parse(entityId);
        if(!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return null;
        }

        return new IdentityType(BuiltInRegistries.ENTITY_TYPE.getValue(id), compound.contains("Variant") ? compound.getIntOr("Variant", -1) : -1);
    }

    public static List<IdentityType<?>> getAllTypes(Level level) {
        if (LIVING_TYPE_CASH.isEmpty()) {
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {

                // Skip if already blacklisted (xGabou: CompatUtils integration)
                Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

                if (CompatUtils.isBlacklistedEntityType(id.toString())) {
                    continue;
                }

                try {
                    // Try to create an instance once for compatibility check
                    Entity instance = type.create(level, EntitySpawnReason.COMMAND);

                    if (instance instanceof LivingEntity) {
                        // Cache only if safe
                        LIVING_TYPE_CASH.add((EntityType<? extends LivingEntity>) type);
                    }

                } catch (Throwable t) {
                    // Mark incompatible so future checks skip instantly
                    CompatUtils.markIncompatibleEntityType(id.toString());
                    Identity.LOGGER.warn("Skipping incompatible identity type {} during cache.", type, t);
                }
            }
        }

        List<IdentityType<?>> types = new ArrayList<>();
        for (EntityType<? extends LivingEntity> type : LIVING_TYPE_CASH) {
            if(VARIANT_BY_TYPE.containsKey(type)) {
                TypeProvider<?> variant = VARIANT_BY_TYPE.get(type);
                for (int i = 0; i <= variant.getRange(); i++) {
                    types.add(new IdentityType<>((EntityType<LivingEntity>) type, i));
                }
            } else {
                types.add(new IdentityType<>((EntityType<LivingEntity>) type));
            }
        }

        return types;
    }

    @Nullable
    public static <Z extends LivingEntity> IdentityType<Z> from(EntityType<?> entityType, int variant) {
        if(VARIANT_BY_TYPE.containsKey(entityType)) {
            TypeProvider<?> provider = VARIANT_BY_TYPE.get(entityType);
            if(variant < -1 || variant > provider.getRange()) {
                return null;
            }
        }

        return new IdentityType<>((EntityType<Z>) entityType, variant);
    }

    public CompoundTag writeCompound() {
        CompoundTag compound = new CompoundTag();
        compound.putString("EntityID", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        compound.putInt("Variant", variantData);
        return compound;
    }

    public EntityType<? extends LivingEntity> getEntityType() {
        return type;
    }

    public T create(Level level) {
        TypeProvider<T> typeProvider = (TypeProvider<T>) VARIANT_BY_TYPE.get(type);
        if(typeProvider != null) {
            return typeProvider.create(type, level, variantData);
        }

        return type.create(level, EntitySpawnReason.COMMAND);
    }

    public int getVariantData() {
        return variantData;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        IdentityType<?> that = (IdentityType<?>) o;
        return variantData == that.variantData && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, variantData);
    }

    public void writeEntityNbt(CompoundTag tag) {
        CompoundTag inner = writeCompound();
        tag.put("IdentityType", inner);
    }

    public static IdentityType<?> fromEntityNbt(CompoundTag tag) {
        return tag.getCompound("IdentityType").map(IdentityType::from).orElse(null);
    }

    public Component createTooltipText(T entity) {
        TypeProvider<T> provider = (TypeProvider<T>) VARIANT_BY_TYPE.get(type);
        if(provider != null) {
            return provider.modifyText(entity, Component.translatable(type.getDescriptionId()));
        }

        return Component.translatable(type.getDescriptionId());
    }
}
