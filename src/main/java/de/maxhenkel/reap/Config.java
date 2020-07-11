package de.maxhenkel.reap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
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
    public static ForgeConfigSpec.BooleanValue DYNAMIC_TREE_BREAKING_ENABLED;
    public static ForgeConfigSpec.DoubleValue DYNAMIC_TREE_BREAKING_MIN_SPEED;
    public static ForgeConfigSpec.DoubleValue DYNAMIC_TREE_BREAKING_PER_LOG;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();
    }

    public static List<ITag<Block>> getReapWhitelist() {
        return REAP_WHITE_LIST.get().stream().map(Config::getBlockTag).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<ITag<Block>> getLogTypes() {
        return LOG_TYPES.get().stream().map(Config::getBlockTag).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<ITag<Block>> getGroundTypes() {
        return GROUND_TYPES.get().stream().map(Config::getBlockTag).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<ITag<Item>> getAllowedTreeTools() {
        return ALLOWED_TREE_TOOLS.get().stream().map(Config::getItemTag).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static class ServerConfig {

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            REAP_WHITE_LIST = builder
                    .comment("The blocks that should get harvested by right-clicking")
                    .defineList("crop_harvesting.whitelist", Arrays.asList(
                            "minecraft:nether_wart",
                            "minecraft:potatoes",
                            "minecraft:carrots",
                            "minecraft:wheat",
                            "minecraft:beetroots",
                            "minecraft:cocoa"
                    ), Objects::nonNull);
            LOG_TYPES = builder
                    .comment("The log blocks that are allowed to get harvested by the tree harvester")
                    .comment("Examples: 'minecraft:oak_log', '#minecraft:logs'")
                    .defineList("tree_harvesting.log_types", Arrays.asList(
                            "minecraft:acacia_log",
                            "minecraft:birch_log",
                            "minecraft:dark_oak_log",
                            "minecraft:jungle_log",
                            "minecraft:oak_log",
                            "minecraft:spruce_log",
                            "minecraft:crimson_stem",
                            "minecraft:warped_stem"
                    ), Objects::nonNull);
            GROUND_TYPES = builder
                    .comment("The blocks that are allowed below logs that can be harvested")
                    .comment("Examples: 'minecraft:dirt', '#forge:sand/colorless'")
                    .defineList("tree_harvesting.ground_types", Arrays.asList(
                            "minecraft:dirt",
                            "minecraft:grass_block",
                            "minecraft:coarse_dirt",
                            "minecraft:podzol",
                            "minecraft:mycelium",
                            "minecraft:warped_nylium",
                            "minecraft:crimson_nylium",
                            "minecraft:netherrack"
                    ), Objects::nonNull);
            ALLOWED_TREE_TOOLS = builder
                    .comment("The tools which the player is allowed to harvest trees")
                    .defineList("tree_harvesting.allowed_tree_tools", Arrays.asList(
                            "minecraft:wooden_axe",
                            "minecraft:golden_axe",
                            "minecraft:stone_axe",
                            "minecraft:iron_axe",
                            "minecraft:diamond_axe",
                            "minecraft:netherite_axe"
                    ), Objects::nonNull);
            TREE_HARVEST = builder
                    .comment("If the tree harvester should be enabled")
                    .define("tree_harvesting.enabled", true);
            DYNAMIC_TREE_BREAKING_ENABLED = builder
                    .comment("If bigger trees should be harder to break")
                    .define("tree_harvesting.dynamic_breaking_speed.enabled", true);
            DYNAMIC_TREE_BREAKING_MIN_SPEED = builder
                    .comment("The maximum amount of time a tree should take to harvest")
                    .defineInRange("tree_harvesting.dynamic_breaking_speed.min_speed", 10D, 1D, 100D);
            DYNAMIC_TREE_BREAKING_PER_LOG = builder
                    .comment("The amount of breaking time that gets added per harvested log")
                    .defineInRange("tree_harvesting.dynamic_breaking_speed.per_log", 0.1D, 0D, 100D);
        }
    }

    private static ITag<Block> getBlockTag(String name) {
        if (name.startsWith("#")) {
            return BlockTags.getCollection().get(new ResourceLocation(name.substring(1)));
        } else {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
            if (block == null) {
                return null;
            } else {
                return new SingleTag<>(block);
            }
        }
    }

    private static ITag<Item> getItemTag(String name) {
        if (name.startsWith("#")) {
            return ItemTags.getCollection().get(new ResourceLocation(name.substring(1)));
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item == null) {
                return null;
            } else {
                return new SingleTag<>(item);
            }
        }
    }
}
