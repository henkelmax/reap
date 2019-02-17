package de.maxhenkel.reap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();
    }

    public static List<Block> reapWhitelist = new ArrayList<>();
    public static List<Block> logTypes = new ArrayList<>();
    public static List<Block> groundTypes = new ArrayList<>();
    public static List<Item> allowedTreeTools = new ArrayList<>();
    public static boolean treeHarvest = true;

    public static void loadServer() {
        reapWhitelist = SERVER.reapWhitelist.get().stream().map(s -> getBlock(s)).filter(b -> b != null).collect(Collectors.toList());
        logTypes = SERVER.logTypes.get().stream().map(s -> getBlock(s)).filter(b -> b != null).collect(Collectors.toList());
        groundTypes = SERVER.groundTypes.get().stream().map(s -> getBlock(s)).filter(b -> b != null).collect(Collectors.toList());
        allowedTreeTools = SERVER.allowedTreeTools.get().stream().map(s -> getItem(s)).filter(b -> b != null).collect(Collectors.toList());
        treeHarvest = SERVER.treeHarvest.get();
    }

    public static class ServerConfig {
        public ForgeConfigSpec.ConfigValue<List<String>> reapWhitelist;
        public ForgeConfigSpec.ConfigValue<List<String>> logTypes;
        public ForgeConfigSpec.ConfigValue<List<String>> groundTypes;
        public ForgeConfigSpec.ConfigValue<List<String>> allowedTreeTools;
        public ForgeConfigSpec.BooleanValue treeHarvest;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            reapWhitelist = builder
                    .comment("")
                    .translation("reap_whitelist")
                    .define("reap_whitelist", Arrays.asList(
                            Blocks.POTATOES, Blocks.CARROTS, Blocks.WHEAT, Blocks.BEETROOTS, Blocks.COCOA
                    ).stream().map(b -> b.getRegistryName().toString()).collect(Collectors.toList()));
            logTypes = builder
                    .comment("")
                    .translation("log_types")
                    .define("log_types", Arrays.asList(
                            Blocks.ACACIA_LOG, Blocks.BIRCH_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG, Blocks.OAK_LOG, Blocks.SPRUCE_LOG
                    ).stream().map(b -> b.getRegistryName().toString()).collect(Collectors.toList()));
            groundTypes = builder
                    .comment("")
                    .translation("ground_types")
                    .define("ground_types", Arrays.asList(
                            Blocks.DIRT, Blocks.GRASS
                    ).stream().map(b -> b.getRegistryName().toString()).collect(Collectors.toList()));
            allowedTreeTools = builder
                    .comment("")
                    .translation("allowed_tree_tools")
                    .define("allowed_tree_tools", Arrays.asList(
                            Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE
                    ).stream().map(i -> i.getRegistryName().toString()).collect(Collectors.toList()));
            treeHarvest = builder
                    .comment("")
                    .translation("tree_harvest")
                    .define("tree_harvest", true);
        }
    }

    @Nullable
    public static Block getBlock(String name) {
        try {
            String[] split = name.split(":");
            if (split.length == 2) {
                Block b = IRegistry.field_212618_g.get(new ResourceLocation(split[0], split[1]));
                if (isAirBlock(b)) {
                    return null;
                } else {
                    return b;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Item getItem(String name) {
        try {
            String[] split = name.split(":");
            if (split.length == 2) {
                Item i = IRegistry.field_212630_s.get(new ResourceLocation(split[0], split[1]));
                if (i.equals(Items.AIR)) {
                    return null;
                } else {
                    return i;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAirBlock(Block block) {
        return block.equals(Blocks.AIR) || block.equals(Blocks.CAVE_AIR) || block.equals(Blocks.VOID_AIR);
    }
}
