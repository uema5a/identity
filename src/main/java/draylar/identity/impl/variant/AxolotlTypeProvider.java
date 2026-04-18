package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class AxolotlTypeProvider extends TypeProvider<Axolotl> {

    @Override
    public int getVariantData(Axolotl entity) {
        return entity.getVariant().getId();
    }

    @Override
    public Axolotl create(EntityType<Axolotl> type, Level level, int data) {
        // Axolotl.setVariant is private in 26.1
        return type.create(level, EntitySpawnReason.COMMAND);
    }

    @Override
    public int getFallbackData() {
        return Axolotl.Variant.LUCY.getId();
    }

    @Override
    public int getRange() {
        return Axolotl.Variant.values().length - 1;
    }

    @Override
    public Component modifyText(Axolotl entity, MutableComponent text) {
        return Component.literal(formatTypePrefix(Axolotl.Variant.values()[getVariantData(entity)].getName()) + " ").append(text);
    }
}
