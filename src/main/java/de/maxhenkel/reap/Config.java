package de.maxhenkel.reap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
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
                            "minecraft:potatoes",
                            "minecraft:carrots",
                            "minecraft:wheat",
                            "minecraft:beetroots",
                            "minecraft:cocoa"
                    ));
            logTypes = builder
                    .comment("")
                    .translation("log_types")
                    .define("log_types", Arrays.asList(
                            "minecraft:acacia_log",
                            "minecraft:birch_log",
                            "minecraft:dark_oak_log",
                            "minecraft:jungle_log",
                            "minecraft:oak_log",
                            "minecraft:spruce_log"
                    ));
            groundTypes = builder
                    .comment("")
                    .translation("ground_types")
                    .define("ground_types", Arrays.asList(
                            "minecraft:dirt",
                            "minecraft:grass_block"
                    ));
            allowedTreeTools = builder
                    .comment("")
                    .translation("allowed_tree_tools")
                    .define("allowed_tree_tools", Arrays.asList(
                            "minecraft:wooden_axe",
                            "minecraft:golden_axe",
                            "minecraft:stone_axe",
                            "minecraft:iron_axe",
                            "minecraft:diamond_axe"
                    ));
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
                Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0], split[1]));
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
                Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
                if (i.getRegistryName().getNamespace().equals("minecraft") && i.getRegistryName().getPath().equals("air")) {
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
        return checkBlock(block, "minecraft", "air") || checkBlock(block, "minecraft", "cave_air") || checkBlock(block, "minecraft", "void_air");
    }

    public static boolean checkBlock(Block block, String domain, String path) {
        return block.getRegistryName().getNamespace().equals(domain) && block.getRegistryName().getPath().equals(path);
    }
}
