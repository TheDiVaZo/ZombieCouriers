package me.thedivazo.zombiecouriers.capability.state;

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

public class StateContainerManager {
    public static final String KEY = "courier_state";

    @CapabilityInject(ICourierStateContainer.class)
    @Nonnull
    @SuppressWarnings("ConstantCondition")
    public static Capability<ICourierStateContainer> CAPABILITY = null;

    public static final ResourceLocation CAPABILITY_NAME =
            new ResourceLocation(ZombieCouriers.MOD_ID, KEY);

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
                ICourierStateContainer.class,
                new SimpleCapabilityStorage<>(),
                CourierStateContainer::new
        );
    }

    public static void onEntityAttachCapabilities(@Nonnull final AttachCapabilitiesEvent<Entity> e) {
        final ICourierStateContainer capability = new CourierStateContainer();
        e.addCapability(
                CAPABILITY_NAME,
                SimpleCapabilityProvider.from(CAPABILITY, () -> capability)
        );
    }

    public static LazyOptional<ICourierStateContainer> getCourierState(@Nonnull final Entity entity) {
        return entity.getCapability(CAPABILITY);
    }
}
