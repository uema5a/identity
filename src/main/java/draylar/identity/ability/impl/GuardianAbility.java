package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class GuardianAbility extends IdentityAbility<Guardian> {

    @Override
    public void onUse(Player player, Guardian identity, Level level) {
        if (!level.isClientSide) {
            List<Player> targets = level.getEntitiesOfClass(
                    Player.class,
                    player.getBoundingBox().inflate(50.0D)
            );

            for (Player target : targets) {
                if (target != player) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 60, 2));
                }
            }
        }
    }

    @Override
    public Item getIcon() {
        return Items.PRISMARINE_SHARD;
    }

    @Override
    public int getCooldown(Guardian entity) {
        return 20 * 30;
    }
}
