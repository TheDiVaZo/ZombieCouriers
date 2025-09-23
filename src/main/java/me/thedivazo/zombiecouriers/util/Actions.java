package me.thedivazo.zombiecouriers.util;

import me.thedivazo.zombiecouriers.ai.Event;
import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.iventory.ICourierInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public enum Actions {;
    public final static StateMachine.EventAction EQUIP_ITEM_ACTION = (entity, event) -> {
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

    public final static StateMachine.EventAction ANIMATE_ACTION = (entity, event) -> {
        if (event == Event.DROP_CROP || event == Event.FARM_CROP) {
            entity.animateHurt();
        }
    };

    public final static StateMachine.EventAction CHANGE_NAME_ACTION = (entity, event) -> {
        if (event == Event.CHANGE_STATE) {
            entity.setCustomName(Messages.CHANGE_STATE);
        }
        else if (event == Event.SEARCH_FARM) {
            entity.setCustomName(Messages.SEARCH_FARM);
        }
        else if (event == Event.FARM_CROP) {
            entity.setCustomName(Messages.FARM_CROP);
        }
        else if (event == Event.GO_TO_FARM) {
            entity.setCustomName(Messages.GO_TO_FARM);
        }
        else if (event == Event.SEARCH_DOOR) {
            entity.setCustomName(Messages.SEARCH_DOOR);
        }
        else if (event == Event.GO_TO_VILLAGE) {
            entity.setCustomName(Messages.GO_TO_VILLAGE);
        }
        else if (event == Event.SEARCH_VILLAGE) {
            entity.setCustomName(Messages.SEARCH_VILLAGE);
        }
        else if (event == Event.GO_TO_NEXT_DOOR) {
            entity.setCustomName(Messages.GO_NEXT_DOOR);
        }
        else if (event == Event.DROP_CROP) {
            entity.setCustomName(Messages.DROP_CROP);
        }
    };
}
