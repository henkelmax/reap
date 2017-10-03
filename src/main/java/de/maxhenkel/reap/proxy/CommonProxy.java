package de.maxhenkel.reap.proxy;

import de.maxhenkel.reap.Events;
import de.maxhenkel.reap.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preinit(FMLPreInitializationEvent event) {
		try{
			new Config(new Configuration(event.getSuggestedConfigurationFile()));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new Events());
	}

	public void postinit(FMLPostInitializationEvent event) {

	}

	
}
