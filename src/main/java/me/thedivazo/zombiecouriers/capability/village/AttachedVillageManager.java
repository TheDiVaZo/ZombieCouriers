package me.thedivazo.zombiecouriers.capability.village;

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

public class AttachedVillageManager {
    public static final String KEY = "attached_village";

    @CapabilityInject(IAttachedVillageContainer.class)
    @Nonnull
    @SuppressWarnings("ConstantCondition")
    public static Capability<IAttachedVillageContainer> CAPABILITY = null;

    public static final ResourceLocation CAPABILITY_NAME =
            new ResourceLocation(ZombieCouriers.MOD_ID, KEY);

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
                IAttachedVillageContainer.class,
                new SimpleCapabilityStorage<>(),
                AttachedVillageContainer::new
        );
    }

    public static void onEntityAttachCapabilities(@Nonnull final AttachCapabilitiesEvent<Entity> e) {
        final IAttachedVillageContainer capability = new AttachedVillageContainer();
        e.addCapability(
                CAPABILITY_NAME,
                SimpleCapabilityProvider.from(CAPABILITY, () -> capability)
        );
    }

    public static LazyOptional<IAttachedVillageContainer> getAttachedVillage(@Nonnull final Entity entity) {
        return entity.getCapability(CAPABILITY);
    }
}
