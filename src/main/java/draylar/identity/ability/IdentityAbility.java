package draylar.identity.ability;

import draylar.identity.Identity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class IdentityAbility<E extends LivingEntity> {

    /**
     * Defines the use action of this ability. Implementers can assume the ability checks, such as cool-downs, have successfully passed.
     *
     * @param player   player using the ability
     * @param identity current identity of the player
     * @param level    world the player is residing in
     */
    abstract public void onUse(Player player, E identity, Level level);

    /**
     * @return cooldown of this ability, in ticks, after it is used.
     */
    public int getCooldown(E entity) {
        return Identity.getCooldown(null); // workaround: or refactor later
    }

    public abstract Item getIcon();
}
