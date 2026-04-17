package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import draylar.identity.mixin.accessor.SlimeEntityAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class SlimeTypeProvider extends TypeProvider<Slime> {

    @Override
    public int getVariantData(Slime entity) {
        return entity.getSize();
    }

    @Override
    public Slime create(EntityType<Slime> type, Level level, int data) {
        Slime slime = new Slime(type, level);
        ((SlimeEntityAccessor) slime).callSetSize(data + 1, true);
        return slime;
    }

    @Override
    public int getFallbackData() {
        return 0;
    }

    @Override
    public int getRange() {
        return 4;
    }

    @Override
    public Component modifyText(Slime entity, MutableComponent text) {
        return Component.literal(String.format("Size %d ", entity.getSize())).append(text);
    }
}
