package me.thedivazo.zombiecouriers.eventhandler;

import me.thedivazo.zombiecouriers.ZombieCouriers;
import me.thedivazo.zombiecouriers.ai.FindVillageGoal;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ZombieCouriers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Class<? extends Goal>[] REMOVE_GOALS = new Class[]{
        ZombieAttackGoal.class,
        //MoveThroughVillageGoal.class,
        HurtByTargetGoal.class,
        NearestAttackableTargetGoal.class,
        BreakBlockGoal.class
    };

    @SubscribeEvent
    public void replaceZombieAI(EntityJoinWorldEvent event) throws IllegalAccessException {
        if (event.getEntity().level.isClientSide || !(event.getEntity() instanceof ZombieEntity) ) return;
        ZombieEntity zombie = (ZombieEntity) event.getEntity();

        removeGoals(zombie.goalSelector);
        removeGoals(zombie.targetSelector);

        zombie.goalSelector.addGoal(1, new FindVillageGoal(zombie));

        zombie.setAggressive(false);
        zombie.clearFire();
        zombie.setRemainingFireTicks(0);
        //zombie.setCanBreakDoors(true);

        LOGGER.info(String.format("Zombie '%s' AI was replaced", zombie.getUUID()));
    }

    private void removeGoals(GoalSelector selector) throws IllegalAccessException {
        Field aviableGoalsField = ObfuscationReflectionHelper.findField(GoalSelector.class, "availableGoals");
        aviableGoalsField.setAccessible(true);
        Set<PrioritizedGoal> goals = (Set<PrioritizedGoal>) aviableGoalsField.get(selector);

        goals.removeIf(prioritizedGoal -> {
            Goal goal = prioritizedGoal.getGoal();

            for (Class<? extends Goal> removeGoal : REMOVE_GOALS) {
                if (removeGoal.isAssignableFrom(goal.getClass())) {
                    return true;
                }
            }
            return false;
        });
    }
}
