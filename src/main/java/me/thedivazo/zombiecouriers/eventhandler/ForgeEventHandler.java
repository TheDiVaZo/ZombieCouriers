package me.thedivazo.zombiecouriers.eventhandler;

import me.thedivazo.zombiecouriers.ZombieCouriers;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.state.StateContainerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = ZombieCouriers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandler {

    @SubscribeEvent
    public void onEntityAttachCapabilities(@Nonnull final AttachCapabilitiesEvent<Entity> attachCapabilitiesEvent) {
        if (attachCapabilitiesEvent.getObject() instanceof ZombieEntity) {
            CourierInventoryManager.onEntityAttachCapabilities(attachCapabilitiesEvent);
            StateContainerManager.onEntityAttachCapabilities(attachCapabilitiesEvent);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ZombieEntity) {
            CourierInventoryManager.getCourierInventory(entity).ifPresent(handler -> {
                for (ItemStack stack : handler.clearAndGet()) {
                    if (!stack.isEmpty()) {
                        event.getDrops().add(new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }
            });
        }
    }
}
