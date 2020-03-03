package de.maxhenkel.reap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> reapWhitelist;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> logTypes;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> groundTypes;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> allowedTreeTools;
    private static ForgeConfigSpec.BooleanValue treeHarvest;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();
    }

    public static List<Block> getReapWhitelist() {
        return reapWhitelist.get().stream().map(Config::getBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Block> getLogTypes() {
        return logTypes.get().stream().map(Config::getBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Block> getGroundTypes() {
        return groundTypes.get().stream().map(Config::getBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Item> getAllowedTreeTools() {
        return allowedTreeTools.get().stream().map(Config::getItem).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static boolean getTreeHarvest() {
        return treeHarvest.get();
    }

    public static class ServerConfig {

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            reapWhitelist = builder
                    .comment("The blocks that should get harvested by right-clicking")
                    .defineList("reap_whitelist", Arrays.asList(
                            "minecraft:nether_wart",
                            "minecraft:potatoes",
                            "minecraft:carrots",
                            "minecraft:wheat",
                            "minecraft:beetroots",
                            "minecraft:cocoa"
                    ), Objects::nonNull);
            logTypes = builder
                    .comment("The log blocks that are allowed to get harvested by the tree harvester")
                    .defineList("log_types", Arrays.asList(
                            "minecraft:acacia_log",
                            "minecraft:birch_log",
                            "minecraft:dark_oak_log",
                            "minecraft:jungle_log",
                            "minecraft:oak_log",
                            "minecraft:spruce_log"
                    ), Objects::nonNull);
            groundTypes = builder
                    .comment("The blocks that are allowed below logs that can be harvested")
                    .defineList("ground_types", Arrays.asList(
                            "minecraft:dirt",
                            "minecraft:grass_block",
                            "minecraft:coarse_dirt",
                            "minecraft:podzol",
                            "minecraft:mycelium"
                    ), Objects::nonNull);
            allowedTreeTools = builder
                    .comment("The tools which the player is allowed to harvest trees")
                    .defineList("allowed_tree_tools", Arrays.asList(
                            "minecraft:wooden_axe",
                            "minecraft:golden_axe",
                            "minecraft:stone_axe",
                            "minecraft:iron_axe",
                            "minecraft:diamond_axe"
                    ), Objects::nonNull);
            treeHarvest = builder
                    .comment("If the tree harvester should be enabled")
                    .define("tree_harvest", true);
        }
    }

    @Nullable
    private static Block getBlock(String name) {
        try {
            String[] split = name.split(":");
            if (split.length == 2) {
                return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0], split[1]));
            } else if (split.length == 1) {
                return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft", split[0]));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Item getItem(String name) {
        try {
            String[] split = name.split(":");
            if (split.length == 2) {
                return ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
            } else if (split.length == 1) {
                return ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", split[0]));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
