package de.maxhenkel.reap;

import de.maxhenkel.corelib.CommonRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "reap";

    public static ServerConfig SERVER_CONFIG;

    public Main(IEventBus eventBus) {
        eventBus.addListener(this::commonSetup);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class, true);
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new CropEvents());
        NeoForge.EVENT_BUS.register(new TreeEvents());
    }

}
