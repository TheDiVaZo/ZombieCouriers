package me.thedivazo.zombiecouriers;

import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.state.StateContainerManager;
import me.thedivazo.zombiecouriers.capability.village.AttachedVillageManager;
import me.thedivazo.zombiecouriers.eventhandler.ForgeEventHandler;
import me.thedivazo.zombiecouriers.eventhandler.ModEventHandler;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ZombieCouriers.MOD_ID)
public class ZombieCouriers
{
    public static final String MOD_ID = "zombiecouriers";

    private static final Logger LOGGER = LogManager.getLogger();

    public ZombieCouriers() {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        CourierInventoryManager.registerCapabilities();
        StateContainerManager.registerCapabilities();
        AttachedVillageManager.registerCapabilities();

        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
