package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.level.Level;

public class CowTypeProvider extends TypeProvider<Cow> {

    @Override
    public int getVariantData(Cow entity) {
        Holder<CowVariant> variant = entity.getVariant();
        if (variant.is(CowVariants.WARM)) return 1;
        if (variant.is(CowVariants.COLD)) return 2;
        return 0;
    }

    @Override
    public Cow create(EntityType<Cow> type, Level level, int data) {
        Cow cow = type.create(level, EntitySpawnReason.COMMAND);
        if (cow != null) {
            var registry = level.registryAccess().lookupOrThrow(Registries.COW_VARIANT);
            Holder<CowVariant> variant = switch (data) {
                case 1 -> registry.getOrThrow(CowVariants.WARM);
                case 2 -> registry.getOrThrow(CowVariants.COLD);
                default -> registry.getOrThrow(CowVariants.TEMPERATE);
            };
            cow.setVariant(variant);
        }
        return cow;
    }

    @Override
    public int getFallbackData() {
        return 0;
    }

    @Override
    public int getRange() {
        return 2;
    }

    @Override
    public Component modifyText(Cow entity, MutableComponent text) {
        return text;
    }
}
