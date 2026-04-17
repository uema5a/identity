package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class WitchAbility extends IdentityAbility<Witch> {

    public static final List<Holder<Potion>> VALID_POTIONS = Arrays.asList(
            Potions.HARMING,
            Potions.POISON,
            Potions.SLOWNESS,
            Potions.WEAKNESS
    );

    @Override
    public void onUse(Player player, Witch identity, Level level) {
        Holder<Potion> chosenPotion = VALID_POTIONS.get(level.random.nextInt(VALID_POTIONS.size()));
        ItemStack potionStack = new ItemStack(Items.SPLASH_POTION);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(chosenPotion));

        ThrownPotion potionEntity = new ThrownPotion(level, player);
        potionEntity.setItem(potionStack);
        potionEntity.setXRot(-20.0F);
        Vec3 rotation = player.getLookAngle();
        potionEntity.shoot(rotation.x, rotation.y, rotation.z, 0.75F, 8.0F);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITCH_THROW, player.getSoundSource(), 1.0F, 0.8F + level.random.nextFloat() * 0.4F);

        level.addFreshEntity(potionEntity);
    }

    @Override
    public Item getIcon() {
        return Items.POTION;
    }
}
