package me.thedivazo.zombiecouriers.eventhandler;

import me.thedivazo.zombiecouriers.ZombieCouriers;
import me.thedivazo.zombiecouriers.ai.OpenDoorForeverGoal;
import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.ai.courier.DistributionGoal;
import me.thedivazo.zombiecouriers.ai.courier.FarmGardenBedGoal;
import me.thedivazo.zombiecouriers.ai.courier.FindVillageGoal;
import me.thedivazo.zombiecouriers.ai.courier.FirstStateSetGoal;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.iventory.ICourierInventory;
import me.thedivazo.zombiecouriers.capability.state.Event;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
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

    private final static StateMachine.EventAction EQUIP_ITEM_ACTION = (entity, event) -> {
        if (event == Event.DROP_CROP) {
            entity.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        else if (event == Event.GO_TO_NEXT_DOOR) {
            Item currentItem = CourierInventoryManager.getCourierInventory(entity)
                    .map(ICourierInventory::peekItemOne)
                    .orElse(Items.AIR);
            if (currentItem != Items.AIR) {
                entity.setItemInHand(Hand.MAIN_HAND, currentItem.getDefaultInstance());
            }
        }
        else if (event == Event.GO_TO_FARM) {
            entity.setItemInHand(Hand.MAIN_HAND, Items.WOODEN_HOE.getDefaultInstance());
        }
    };

    private final static StateMachine.EventAction ANIMATE_ACTION = (entity, event) -> {
        if (event == Event.DROP_CROP || event == Event.FARM_CROP) {
            entity.animateHurt();
        }
    };

    private final static StringTextComponent CHANGE_STATE = new StringTextComponent("state has been changed");
    private final static StringTextComponent FARM_CROP = new StringTextComponent("crop is farmed");
    private final static StringTextComponent GO_TO_FARM = new StringTextComponent("go to farm");
    private final static StringTextComponent GO_NEXT_DOOR = new StringTextComponent("go to next door");
    private final static StringTextComponent DROP_CROP = new StringTextComponent("drop crop");
    private final static StringTextComponent SEARCH_DOOR = new StringTextComponent("search doors");
    private final static StringTextComponent SEARCH_FARM = new StringTextComponent("search farm");
    private final static StringTextComponent GO_TO_VILLAGE = new StringTextComponent("go to village");
    private final static StringTextComponent SEARCH_VILLAGE = new StringTextComponent("search vallage");
    private final static StateMachine.EventAction CHANGE_NAME_ACTION = (entity, event) -> {
        if (event == Event.CHANGE_STATE) {
            entity.setCustomName(CHANGE_STATE);
        }
        else if (event == Event.SEARCH_FARM) {
            entity.setCustomName(SEARCH_FARM);
        }
        else if (event == Event.FARM_CROP) {
            entity.setCustomName(FARM_CROP);
        }
        else if (event == Event.GO_TO_FARM) {
            entity.setCustomName(GO_TO_FARM);
        }
        else if (event == Event.SEARCH_DOOR) {
            entity.setCustomName(SEARCH_DOOR);
        }
        else if (event == Event.GO_TO_VILLAGE) {
            entity.setCustomName(GO_TO_VILLAGE);
        }
        else if (event == Event.SEARCH_VILLAGE) {
            entity.setCustomName(SEARCH_VILLAGE);
        }
        else if (event == Event.GO_TO_NEXT_DOOR) {
            entity.setCustomName(GO_NEXT_DOOR);
        }
        else if (event == Event.DROP_CROP) {
            entity.setCustomName(DROP_CROP);
        }
    };

    @SubscribeEvent
    public void replaceZombieAI(EntityJoinWorldEvent event) throws IllegalAccessException {
        if (event.getEntity().level.isClientSide || !(event.getEntity() instanceof ZombieEntity) ) return;
        ZombieEntity zombie = (ZombieEntity) event.getEntity();
        removeGoals(zombie.goalSelector);
        removeGoals(zombie.targetSelector);

        StateMachine stateMachine = new StateMachine(zombie, EQUIP_ITEM_ACTION, ANIMATE_ACTION, CHANGE_NAME_ACTION);

        zombie.goalSelector.addGoal(0, new OpenDoorForeverGoal(zombie, false));
        zombie.goalSelector.addGoal(1, new FirstStateSetGoal(zombie, stateMachine));
        zombie.goalSelector.addGoal(2, new FindVillageGoal(zombie, stateMachine));
        zombie.goalSelector.addGoal(2, new FarmGardenBedGoal(zombie, stateMachine));
        zombie.goalSelector.addGoal(2, new DistributionGoal(zombie, stateMachine));

        ((GroundPathNavigator) zombie.getNavigation()).setCanOpenDoors(true);

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
