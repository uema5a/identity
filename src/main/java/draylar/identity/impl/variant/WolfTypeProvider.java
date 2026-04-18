package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;

public class WolfTypeProvider extends TypeProvider<Wolf> {

    @Override
    public int getVariantData(Wolf entity) {
        // Wolf variants are registry-based holders with private accessors; return 0 as default
        return 0;
    }

    @Override
    public Wolf create(EntityType<Wolf> type, Level level, int data) {
        // Wolf.setVariant() is private in 26.1; use default spawn which sets PALE variant
        return type.create(level, EntitySpawnReason.COMMAND);
    }

    @Override
    public int getFallbackData() {
        return 0;
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public Component modifyText(Wolf entity, MutableComponent text) {
        return text;
    }
}
