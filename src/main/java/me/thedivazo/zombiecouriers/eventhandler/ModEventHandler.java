package me.thedivazo.zombiecouriers.eventhandler;

import me.thedivazo.zombiecouriers.ZombieCouriers;
import me.thedivazo.zombiecouriers.ai.LeavesBreakGoal;
import me.thedivazo.zombiecouriers.ai.OpenDoorForeverGoal;
import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.ai.courier.DistributionGoal;
import me.thedivazo.zombiecouriers.ai.courier.FarmGardenBedGoal;
import me.thedivazo.zombiecouriers.ai.courier.FindVillageGoal;
import me.thedivazo.zombiecouriers.ai.courier.FirstStateSetGoal;
import me.thedivazo.zombiecouriers.util.Actions;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNodeType;
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

        StateMachine stateMachine = new StateMachine(
                zombie,
                Actions.EQUIP_ITEM_ACTION,
                Actions.ANIMATE_ACTION,
                Actions.CHANGE_NAME_ACTION
        );

        zombie.goalSelector.addGoal(0, new OpenDoorForeverGoal(zombie));
        zombie.goalSelector.addGoal(0, new LeavesBreakGoal(zombie));

        zombie.goalSelector.addGoal(1, new FirstStateSetGoal(zombie, stateMachine));
        zombie.goalSelector.addGoal(2, new FindVillageGoal(zombie, stateMachine));
        zombie.goalSelector.addGoal(2, new FarmGardenBedGoal(zombie, stateMachine));
        zombie.goalSelector.addGoal(2, new DistributionGoal(zombie, stateMachine));

        ((GroundPathNavigator) zombie.getNavigation()).setCanOpenDoors(true);
        zombie.setPathfindingMalus(PathNodeType.DOOR_WOOD_CLOSED, 0.0F);
        zombie.setPathfindingMalus(PathNodeType.LEAVES, 0.0F);

        zombie.maxUpStep = 1.1F;
        zombie.setAggressive(false);
        zombie.setCustomNameVisible(true);
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
