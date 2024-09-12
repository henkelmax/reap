package de.maxhenkel.reap;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import javax.annotation.Nullable;
import java.util.*;

public class TreeEvents {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level) event.getLevel();
        if (!Main.SERVER_CONFIG.treeHarvest.get() || world.isClientSide()) {
            return;
        }
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        if (canHarvest(player, pos)) {
            destroyTree(player, world, pos);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Main.SERVER_CONFIG.treeHarvest.get() || !Main.SERVER_CONFIG.dynamicTreeBreakingEnabled.get()) {
            return;
        }
        Player player = event.getEntity();
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) {
            return;
        }
        if (canHarvest(player, pos)) {
            LinkedList<BlockPos> connectedLogs = scanForTree(player.level(), pos);
            event.setNewSpeed((float) (event.getOriginalSpeed() / Math.min(1D + Main.SERVER_CONFIG.dynamicTreeBreakingPerLog.get() * connectedLogs.size(), Main.SERVER_CONFIG.dynamicTreeBreakingMinSpeed.get())));
        }
    }

    public static boolean canHarvest(Player player, BlockPos pos) {
        ItemStack heldItem = player.getMainHandItem();
        Level level = player.level();
        if (player.getAbilities().instabuild) {
            return false;
        }

        if (player.isShiftKeyDown()) {
            return false;
        }

        if (Main.SERVER_CONFIG.allowedTreeTools.stream().noneMatch(tag -> tag.contains(heldItem.getItem()))) {
            return false;
        }

        if (!isLog(level, pos)) {
            return false;
        }

        if (!isGround(level, pos.below())) {
            return false;
        }

        return true;
    }

    private static boolean isLog(Level world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Main.SERVER_CONFIG.logTypes.stream().anyMatch(tag -> tag.contains(b.getBlock())) || b.is(BlockTags.LOGS);
    }

    private static boolean isGround(Level world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Main.SERVER_CONFIG.groundTypes.stream().anyMatch(tag -> tag.contains(b.getBlock()));
    }

    private void destroyTree(Player player, Level world, BlockPos pos) {
        LinkedList<BlockPos> connectedLogs = scanForTree(world, pos);
        for (BlockPos log : connectedLogs) {
            destroy(world, player, log);
        }
    }

    private static void destroy(Level world, Player player, BlockPos pos) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem != null) {
            heldItem.getItem().mineBlock(heldItem, world, world.getBlockState(pos), pos, player);
            world.destroyBlock(pos, true);
            player.causeFoodExhaustion(0.025F);
        }
    }

    // tree detector
    public static LinkedList<BlockPos> scanForTree(LevelReader level, BlockPos start) {
        if (!isLog((Level) level, start)) {
            return new LinkedList<>();
        }

        boolean[] leavesFound = new boolean[1];
        LinkedList<BlockPos> result =
                recursiveSearch(level, start, (pos, bs, isRightBlock) -> {
                    if (isLeaves(bs)) {
                        leavesFound[0] = true;
                    }
                    return true;
                });
        return leavesFound[0] ? result : new LinkedList<>();
    }

    // for internal use
    private interface BlockAction {
        boolean onBlock(BlockPos pos, BlockState state, boolean isRightBlock);
    }

    // Recursively scan 3x3x3 cubes while keeping track of already scanned blocks to avoid cycles.
    private static LinkedList<BlockPos> recursiveSearch(LevelReader world, BlockPos start, @Nullable BlockAction action) {
        Block wantedBlock = world.getBlockState(start).getBlock();
        boolean abort = false;
        LinkedList<BlockPos> result = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        LinkedList<BlockPos> queue = new LinkedList<>();
        int maxHarvestingCount = Main.SERVER_CONFIG.treeHarvestMaxCount.get();
        queue.push(start);

        while (!queue.isEmpty()) {
            BlockPos center = queue.pop();
            int x0 = center.getX();
            int y0 = center.getY();
            int z0 = center.getZ();
            for (int z = z0 - 1; z <= z0 + 1 && !abort; ++z) {
                for (int y = y0 - 1; y <= y0 + 1 && !abort; ++y) {
                    for (int x = x0 - 1; x <= x0 + 1 && !abort; ++x) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState bs = world.getBlockState(pos);
                        if ((bs.isAir() || !visited.add(pos))) {
                            continue;
                        }
                        boolean isRightBlock = bs.is(wantedBlock);
                        if (isRightBlock) {
                            result.add(pos);
                            if (queue.size() > maxHarvestingCount) {
                                abort = true;
                                break;
                            }
                            queue.push(pos);
                        }
                        if (action != null) {
                            abort = !action.onBlock(pos, bs, isRightBlock);
                        }
                    }
                }
            }
        }
        return !abort ? result : new LinkedList<>();
    }

    // leaves detector
    // Naturally generated leaves don't have BlockStateProperties.PERSISTENT property, meaning it's a real tree.
    // If there is BlockStateProperties.PERSISTENT property, it means the leave block is placed by player, meaning it's not a natural tree.
    private static boolean isLeaves(BlockState blockState) {
        if (blockState.getBlock() instanceof LeavesBlock) {
            Collection<Property<?>> properties = blockState.getProperties();
            if (properties.contains(BlockStateProperties.PERSISTENT)) {
                return !blockState.getValue(BlockStateProperties.PERSISTENT);
            }
            return true;
        } else {
            return blockState.is(BlockTags.WART_BLOCKS);
        }
    }
}