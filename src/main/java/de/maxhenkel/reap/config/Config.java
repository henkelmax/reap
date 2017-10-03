package de.maxhenkel.reap.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraftforge.common.config.Configuration;

public class Config {

	public static List<BlockSelector> reapWhitelist=new ArrayList<BlockSelector>();
	public static List<BlockSelector> logTypes=new ArrayList<BlockSelector>();
	public static List<BlockSelector> groundTypes=new ArrayList<BlockSelector>();
	public static List<ItemStackSelector> allowedTreeTools=new ArrayList<ItemStackSelector>();
	public static boolean enableTreeHarvest=true;
	
	public Config(Configuration cfg) {
		enableTreeHarvest=cfg.getBoolean("enable_tree_harvest", "", true, "");
		reapWhitelist=getBlockList(cfg, "whitelist", "", "", new BlockSelector[]{new BlockSelector(Blocks.POTATOES), 
				new BlockSelector(Blocks.CARROTS), new BlockSelector(Blocks.WHEAT), new BlockSelector(Blocks.BEETROOTS), new BlockSelector(Blocks.COCOA)});
		
		logTypes=getBlockList(cfg, "log_types", "", "", new BlockSelector[]{new BlockSelector(Blocks.LOG), new BlockSelector(Blocks.LOG2)});
		groundTypes=getBlockList(cfg, "tree_ground_blocks", "", "", new BlockSelector[]{new BlockSelector(Blocks.DIRT), new BlockSelector(Blocks.GRASS)});
		
		allowedTreeTools=getStackList(cfg, "allowed_tree_tools", "", "", new ItemStackSelector[]{new ItemStackSelector(Items.WOODEN_AXE), 
				new ItemStackSelector(Items.GOLDEN_AXE), new ItemStackSelector(Items.STONE_AXE), new ItemStackSelector(Items.IRON_AXE), new ItemStackSelector(Items.DIAMOND_AXE)});
		
		cfg.save();
	}
	
	public static List<ItemStackSelector> getStackList(Configuration config, String name, String category,
			String comment, ItemStackSelector[] defaultValues) {

		String[] def = new String[defaultValues.length];
		for (int i = 0; i < def.length; i++) {
			def[i] = defaultValues[i].toString();
		}

		List<ItemStackSelector> stackList = new ArrayList<ItemStackSelector>();
		String[] array = config.getStringList(name, category, def, comment);

		if (array == null) {
			return stackList;
		}

		for (String s : array) {
			ItemStackSelector selector = ItemStackSelector.fromString(s);
			if (selector != null) {
				stackList.add(selector);
			}
		}

		return stackList;
	}

	public static List<BlockSelector> getBlockList(Configuration config, String name, String category,
			String comment, BlockSelector[] defaultValues) {

		String[] def = new String[defaultValues.length];
		for (int i = 0; i < def.length; i++) {
			def[i] = defaultValues[i].toString();
		}

		List<BlockSelector> stackList = new ArrayList<BlockSelector>();
		String[] array = config.getStringList(name, category, def, comment);

		if (array == null) {
			return stackList;
		}

		for (String s : array) {
			BlockSelector selector = BlockSelector.fromString(s);
			if (selector != null) {
				stackList.add(selector);
			}
		}

		return stackList;
	}
}
