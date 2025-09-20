package me.thedivazo.zombiecouriers.capability.iventory;

import me.thedivazo.zombiecouriers.ZombieCouriers;
import me.thedivazo.zombiecouriers.capability.SimpleCapabilityProvider;
import me.thedivazo.zombiecouriers.capability.SimpleCapabilityStorage;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;

public class CourierInventoryManager {
    public static final String KEY = "courier_inventory";

    @CapabilityInject(ICourierInventory.class)
    @Nonnull
    @SuppressWarnings("ConstantCondition")
    public static Capability<ICourierInventory> CAPABILITY = null;

    public static final ResourceLocation CAPABILITY_NAME =
            new ResourceLocation(ZombieCouriers.MOD_ID, KEY);

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
                ICourierInventory.class,
                new SimpleCapabilityStorage<>(),
                CourierInventory::new
        );
    }

    public static void onEntityAttachCapabilities(@Nonnull final AttachCapabilitiesEvent<Entity> e) {
        final ICourierInventory capability = new CourierInventory();
        e.addCapability(
                CAPABILITY_NAME,
                SimpleCapabilityProvider.from(CAPABILITY, () -> capability)
        );
    }

    public static LazyOptional<ICourierInventory> getCourierInventory(@Nonnull final Entity entity) {
        return entity.getCapability(CAPABILITY);
    }
}
