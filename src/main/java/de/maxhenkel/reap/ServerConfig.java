package de.maxhenkel.reap;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.corelib.tag.TagUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServerConfig extends ConfigBase {

    private final ModConfigSpec.ConfigValue<List<? extends String>> reapWhitelistSpec;
    private final ModConfigSpec.ConfigValue<List<? extends String>> logTypesSpec;
    private final ModConfigSpec.ConfigValue<List<? extends String>> groundTypesSpec;
    private final ModConfigSpec.ConfigValue<List<? extends String>> allowedTreeToolsSpec;
    public final ModConfigSpec.BooleanValue considerTool;
    public final ModConfigSpec.BooleanValue treeHarvest;
    public final ModConfigSpec.IntValue treeHarvestMaxCount;
    public final ModConfigSpec.BooleanValue dynamicTreeBreakingEnabled;
    public final ModConfigSpec.DoubleValue dynamicTreeBreakingMinSpeed;
    public final ModConfigSpec.DoubleValue dynamicTreeBreakingPerLog;

    public List<Tag<Block>> reapWhitelist;
    public List<Tag<Block>> logTypes;
    public List<Tag<Block>> groundTypes;
    public List<Tag<Item>> allowedTreeTools;

    public ServerConfig(ModConfigSpec.Builder builder) {
        super(builder);
        reapWhitelistSpec = builder
                .comment("The blocks that should get harvested by right-clicking")
                .defineList("crop_harvesting.whitelist", Arrays.asList(
                        "#minecraft:crops"
                ), Objects::nonNull);
        logTypesSpec = builder
                .comment("The log blocks that are allowed to get harvested by the tree harvester")
                .comment("Examples: 'minecraft:oak_log', '#minecraft:logs'")
                .defineList("tree_harvesting.log_types", Arrays.asList(
                        "#minecraft:logs"
                ), Objects::nonNull);
        groundTypesSpec = builder
                .comment("The blocks that are allowed below logs that can be harvested")
                .comment("Examples: 'minecraft:dirt', '#forge:sand/colorless'")
                .defineList("tree_harvesting.ground_types", Arrays.asList(
                        "#minecraft:dirt",
                        "#minecraft:sand",
                        "#minecraft:nylium",
                        "minecraft:mycelium"
                ), Objects::nonNull);
        allowedTreeToolsSpec = builder
                .comment("The tools which the player is allowed to harvest trees")
                .defineList("tree_harvesting.allowed_tree_tools", Arrays.asList(
                        "#minecraft:axes",
                        "#forge:axes",
                        "#forge:tools/paxels"
                ), Objects::nonNull);
        considerTool = builder
                .comment("If the held tool should be considered when right-click harvesting")
                .define("crop_harvesting.consider_tool", true);
        treeHarvest = builder
                .comment("If the tree harvester should be enabled")
                .define("tree_harvesting.enabled", true);
        treeHarvestMaxCount = builder
                .comment("The maximum amount of logs one harvest is allowed to do")
                .defineInRange("tree_harvesting.max_harvesting_count", 128, 0, 1024);
        dynamicTreeBreakingEnabled = builder
                .comment("If bigger trees should be harder to break")
                .define("tree_harvesting.dynamic_breaking_speed.enabled", true);
        dynamicTreeBreakingMinSpeed = builder
                .comment("The maximum amount of time a tree should take to harvest")
                .defineInRange("tree_harvesting.dynamic_breaking_speed.min_speed", 10D, 1D, 100D);
        dynamicTreeBreakingPerLog = builder
                .comment("The amount of breaking time that gets added per harvested log")
                .defineInRange("tree_harvesting.dynamic_breaking_speed.per_log", 0.1D, 0D, 100D);
    }

    @Override
    public void onReload(ModConfigEvent event) {
        super.onReload(event);
        reapWhitelist = reapWhitelistSpec.get().stream().map(s -> TagUtils.getBlock(s, true)).filter(Objects::nonNull).collect(Collectors.toList());
        logTypes = logTypesSpec.get().stream().map(s -> TagUtils.getBlock(s, true)).filter(Objects::nonNull).collect(Collectors.toList());
        groundTypes = groundTypesSpec.get().stream().map(s -> TagUtils.getBlock(s, true)).filter(Objects::nonNull).collect(Collectors.toList());
        allowedTreeTools = allowedTreeToolsSpec.get().stream().map(s -> TagUtils.getItem(s, true)).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
