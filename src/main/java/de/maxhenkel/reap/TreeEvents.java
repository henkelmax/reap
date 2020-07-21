package de.maxhenkel.reap;

import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class TreeEvents {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = (World) event.getWorld();
        if (!Main.SERVER_CONFIG.treeHarvest.get() || world.isRemote()) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        BlockPos pos = event.getPos();
        ItemStack heldItem = player.getHeldItemMainhand();
        if (canHarvest(pos, player, world, heldItem)) {
            destroyTree(player, world, pos, heldItem);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Main.SERVER_CONFIG.treeHarvest.get() || !Main.SERVER_CONFIG.dynamicTreeBreakingEnabled.get()) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        BlockPos pos = event.getPos();
        if (pos == null) {
            return;
        }
        if (canHarvest(pos, player, player.world, player.getHeldItemMainhand())) {
            List<BlockPos> connectedLogs = getConnectedLogs(player.world, pos);
            event.setNewSpeed((float) (event.getOriginalSpeed() / Math.min(1D + Main.SERVER_CONFIG.dynamicTreeBreakingPerLog.get() * connectedLogs.size(), Main.SERVER_CONFIG.dynamicTreeBreakingMinSpeed.get())));
        }
    }

    public static boolean canHarvest(BlockPos pos, PlayerEntity player, World world, ItemStack heldItem) {
        if (player.abilities.isCreativeMode) {
            return false;
        }

        if (player.isSneaking()) {
            return false;
        }

        if (Main.SERVER_CONFIG.allowedTreeTools.stream().noneMatch(tag -> tag.func_230235_a_(heldItem.getItem()))) {
            return false;
        }

        if (!isLog(world, pos)) {
            return false;
        }

        if (!isGround(world, pos.down())) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.func_235904_r_().stream().anyMatch(p -> p.equals(RotatedPillarBlock.AXIS))) {
            if (!state.get(RotatedPillarBlock.AXIS).equals(Direction.Axis.Y)) {
                return false;
            }
        }

        return true;
    }

    private static void destroyTree(PlayerEntity player, World world, BlockPos pos, ItemStack heldItem) {
        List<BlockPos> connectedLogs = getConnectedLogs(world, pos);

        for (BlockPos logPos : connectedLogs) {
            destroy(world, player, logPos, heldItem);
        }
    }

    private static List<BlockPos> getConnectedLogs(World world, BlockPos pos) {
        BlockPosList positions = new BlockPosList();
        collectLogs(world, pos, positions);
        return positions;
    }

    private static void collectLogs(World world, BlockPos pos, BlockPosList positions) {
        if (positions.size() >= 128) {
            return;
        }
        List<BlockPos> posList = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = pos.add(x, y, z);
                    if (isLog(world, p)) {
                        if (positions.size() <= 128) {
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

    private static boolean isLog(World world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Main.SERVER_CONFIG.logTypes.stream().anyMatch(tag -> tag.func_230235_a_(b.getBlock()));
    }

    private static boolean isGround(World world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Main.SERVER_CONFIG.groundTypes.stream().anyMatch(tag -> tag.func_230235_a_(b.getBlock()));
    }

    private static void destroy(World world, PlayerEntity player, BlockPos pos, ItemStack heldItem) {
        if (heldItem != null) {
            heldItem.getItem().onBlockDestroyed(heldItem, world, world.getBlockState(pos), pos, player);
            world.destroyBlock(pos, true);
            player.addExhaustion(0.025F);
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