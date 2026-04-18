package draylar.identity.mixin.accessor;

import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TODO Phase D: canLevelUp/fillRecipes/levelUp invokers removed - methods renamed/removed in MC 26.1
// canLevelUp -> VillagerData.canLevelUp() static, fillRecipes -> updateTrades, levelUp -> increaseMerchantCareer
@Mixin(Villager.class)
public interface VillagerEntityAccessor {

    @Accessor("villagerXp")
    int getExperience();

    @Accessor("villagerXp")
    void setExperience(int value);
}

