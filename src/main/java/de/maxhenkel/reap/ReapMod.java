package de.maxhenkel.reap;

import de.maxhenkel.corelib.CommonRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ReapMod.MODID)
@EventBusSubscriber(modid = ReapMod.MODID)
public class ReapMod {

    public static final String MODID = "reap";

    public static ServerConfig SERVER_CONFIG;

    public ReapMod(IEventBus eventBus) {
        SERVER_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.SERVER, ServerConfig.class, true);
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new CropEvents());
        NeoForge.EVENT_BUS.register(new TreeEvents());
    }

}
