package de.maxhenkel.reap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;

public class TreeEvents {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level) event.getLevel();
        if (!ReapMod.SERVER_CONFIG.treeHarvest.get() || world.isClientSide()) {
            return;
        }
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        ItemStack heldItem = player.getMainHandItem();
        if (canHarvest(pos, player, world, heldItem)) {
            destroyTree(player, world, pos, heldItem);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!ReapMod.SERVER_CONFIG.treeHarvest.get() || !ReapMod.SERVER_CONFIG.dynamicTreeBreakingEnabled.get()) {
            return;
        }
        Player player = event.getEntity();
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) {
            return;
        }
        if (canHarvest(pos, player, player.level(), player.getMainHandItem())) {
            List<BlockPos> connectedLogs = getConnectedLogs(player.level(), pos);
            event.setNewSpeed((float) (event.getOriginalSpeed() / Math.min(1D + ReapMod.SERVER_CONFIG.dynamicTreeBreakingPerLog.get() * connectedLogs.size(), ReapMod.SERVER_CONFIG.dynamicTreeBreakingMinSpeed.get())));
        }
    }

    public static boolean canHarvest(BlockPos pos, Player player, Level world, ItemStack heldItem) {
        if (player.getAbilities().instabuild) {
            return false;
        }

        if (player.isShiftKeyDown()) {
            return false;
        }

        if (ReapMod.SERVER_CONFIG.allowedTreeTools.stream().noneMatch(tag -> tag.contains(heldItem.getItem()))) {
            return false;
        }

        if (!isLog(world, pos)) {
            return false;
        }

        if (!isGround(world, pos.below())) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.getProperties().stream().anyMatch(p -> p.equals(RotatedPillarBlock.AXIS))) {
            if (!state.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.Y)) {
                return false;
            }
        }

        return true;
    }

    private static void destroyTree(Player player, Level world, BlockPos pos, ItemStack heldItem) {
        List<BlockPos> connectedLogs = getConnectedLogs(world, pos);

        for (BlockPos logPos : connectedLogs) {
            destroy(world, player, logPos, heldItem);
        }
    }

    private static List<BlockPos> getConnectedLogs(Level world, BlockPos pos) {
        BlockPosList positions = new BlockPosList();
        collectLogs(world, pos, positions);
        return positions;
    }

    private static void collectLogs(Level world, BlockPos pos, BlockPosList positions) {
        int maxHarvestingCount = ReapMod.SERVER_CONFIG.treeHarvestMaxCount.get();
        if (positions.size() >= maxHarvestingCount) {
            return;
        }
        List<BlockPos> posList = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    if (isLog(world, p)) {
                        if (positions.size() <= maxHarvestingCount) {
                            if (positions.add(p)) {
                                posList.add(p);
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        for (BlockPos p : posList) {
            collectLogs(world, p, positions);
        }
    }

    private static boolean isLog(Level world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return ReapMod.SERVER_CONFIG.logTypes.stream().anyMatch(tag -> tag.contains(b.getBlock()));
    }

    private static boolean isGround(Level world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return ReapMod.SERVER_CONFIG.groundTypes.stream().anyMatch(tag -> tag.contains(b.getBlock()));
    }

    private static void destroy(Level world, Player player, BlockPos pos, ItemStack heldItem) {
        if (heldItem != null) {
            heldItem.getItem().mineBlock(heldItem, world, world.getBlockState(pos), pos, player);
            world.destroyBlock(pos, true);
            player.causeFoodExhaustion(0.025F);
        }
    }

    private static class BlockPosList extends ArrayList<BlockPos> {
        @Override
        public boolean add(BlockPos pos) {
            if (!contains(pos)) {
                return super.add(pos);
            }
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return stream().anyMatch(pos1 -> pos1.equals(o));
        }
    }

}