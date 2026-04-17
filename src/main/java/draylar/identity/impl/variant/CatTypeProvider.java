package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class CatTypeProvider extends TypeProvider<Cat> {

    @Override
    public int getVariantData(Cat entity) {
        // Cat variants are now registry-based (Holder<CatVariant>), return 0 as default
        return 0;
    }

    @Override
    public Cat create(EntityType<Cat> type, Level level, int data) {
        return type.create(level, EntitySpawnReason.COMMAND);
    }

    @Override
    public int getFallbackData() {
        return 0;
    }

    @Override
    public int getRange() {
        return 0; // Simplified - cat variants are now registry-based
    }

    @Override
    public Component modifyText(Cat cat, MutableComponent text) {
        return text;
    }
}
