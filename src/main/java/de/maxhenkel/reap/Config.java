package de.maxhenkel.reap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> REAP_WHITE_LIST;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPES;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> GROUND_TYPES;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_TREE_TOOLS;
    public static ForgeConfigSpec.BooleanValue TREE_HARVEST;
    public static ForgeConfigSpec.BooleanValue DYNAMIC_TREE_BREAKING_SPEED;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();
    }

    public static List<Block> getReapWhitelist() {
        return REAP_WHITE_LIST.get().stream().map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Block> getLogTypes() {
        return LOG_TYPES.get().stream().map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Block> getGroundTypes() {
        return GROUND_TYPES.get().stream().map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Item> getAllowedTreeTools() {
        return ALLOWED_TREE_TOOLS.get().stream().map(ResourceLocation::new).map(ForgeRegistries.ITEMS::getValue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static class ServerConfig {

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            REAP_WHITE_LIST = builder
                    .comment("The blocks that should get harvested by right-clicking")
                    .defineList("reap_whitelist", Arrays.asList(
                            "minecraft:nether_wart",
                            "minecraft:potatoes",
                            "minecraft:carrots",
                            "minecraft:wheat",
                            "minecraft:beetroots",
                            "minecraft:cocoa"
                    ), Objects::nonNull);
            LOG_TYPES = builder
                    .comment("The log blocks that are allowed to get harvested by the tree harvester")
                    .defineList("log_types", Arrays.asList(
                            "minecraft:acacia_log",
                            "minecraft:birch_log",
                            "minecraft:dark_oak_log",
                            "minecraft:jungle_log",
                            "minecraft:oak_log",
                            "minecraft:spruce_log"
                    ), Objects::nonNull);
            GROUND_TYPES = builder
                    .comment("The blocks that are allowed below logs that can be harvested")
                    .defineList("ground_types", Arrays.asList(
                            "minecraft:dirt",
                            "minecraft:grass_block",
                            "minecraft:coarse_dirt",
                            "minecraft:podzol",
                            "minecraft:mycelium"
                    ), Objects::nonNull);
            ALLOWED_TREE_TOOLS = builder
                    .comment("The tools which the player is allowed to harvest trees")
                    .defineList("allowed_tree_tools", Arrays.asList(
                            "minecraft:wooden_axe",
                            "minecraft:golden_axe",
                            "minecraft:stone_axe",
                            "minecraft:iron_axe",
                            "minecraft:diamond_axe"
                    ), Objects::nonNull);
            TREE_HARVEST = builder
                    .comment("If the tree harvester should be enabled")
                    .define("tree_harvest", true);
            DYNAMIC_TREE_BREAKING_SPEED = builder
                    .comment("If bigger trees should be harder to break")
                    .define("dynamic_tree_breaking_speed", true);
        }
    }
}
