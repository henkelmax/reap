package de.maxhenkel.reap;

import de.maxhenkel.reap.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION, acceptedMinecraftVersions=Main.MC_VERSION, updateJSON=Main.UPDATE_JSON)
public class Main{
	
    public static final String MODID = "reap";
    public static final String VERSION = "1.5.1";
    public static final String MC_VERSION = "[1.12.2]";
	public static final String UPDATE_JSON = "http://maxhenkel.de/update/reap.json";

	@Instance
    private static Main instance;

	@SidedProxy(clientSide="de.maxhenkel.reap.proxy.ClientProxy", serverSide="de.maxhenkel.reap.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event){
		instance=this;
		proxy.preinit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event){
    	 proxy.init(event);
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event){
		proxy.postinit(event);
    }

    public static Main instance() {
		return instance;
	}
    
}
