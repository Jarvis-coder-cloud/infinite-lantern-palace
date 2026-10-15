package com.lanternpalace.infinitycastle;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Makes the Infinity Castle a sanctuary, enforced on the server:
 *  - no creature or player can deal damage to another living entity (no PvP, no mob attacks, no hitting animals);
 *    environmental damage (falling, fire...) still works;
 *  - hostile mobs ({@link Enemy}) are removed the moment they enter the dimension, however they arrived
 *    (spawn eggs, spawners, commands, portals). The biome also has no monster spawns, only friendly animals.
 */
public final class PeacefulRules {
    private PeacefulRules() {}

    /** Set to false to let players hit animals (still no PvP / mob attacks). */
    public static final boolean PROTECT_ANIMALS_FROM_PLAYERS = true;

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!entity.level().dimension().equals(Dimensions.CASTLE)) return true;
            if (!(source.getEntity() instanceof LivingEntity attacker)) return true; // environmental damage
            if (attacker instanceof net.minecraft.world.entity.player.Player && !(entity instanceof net.minecraft.world.entity.player.Player)
                    && !PROTECT_ANIMALS_FROM_PLAYERS) return true;
            return false;
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (level.dimension().equals(Dimensions.CASTLE) && entity instanceof Enemy) entity.discard();
        });
    }
}
