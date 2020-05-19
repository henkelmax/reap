package de.maxhenkel.reap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeHarvester {

    public static void harvest(BlockPos pos, PlayerEntity player, World world) {
        if (!Config.getTreeHarvest()) {
            return;
        }

        ItemStack heldItem = player.getHeldItemMainhand();

        if (player.isSneaking()) {
            return;
        }

        if (Config.getAllowedTreeTools().stream().noneMatch(i -> i.equals(heldItem.getItem()))) {
            return;
        }

        if (!isLog(world, pos)) {
            return;
        }

        if (!isGround(world, pos.down())) {
            return;
        }

        if (!world.getBlockState(pos).get(LogBlock.AXIS).equals(Direction.Axis.Y)) {
            return;
        }

        destroyTree(world, player, pos, heldItem);
    }

    private static void destroyTree(World world, PlayerEntity player, BlockPos pos, ItemStack heldItem) {
        destroyConnectedLogs(world, player, pos, heldItem, new AtomicInteger(128));
    }

    private static void destroyConnectedLogs(World world, PlayerEntity player, BlockPos pos, ItemStack heldItem, AtomicInteger counter) {
        if (counter.get() <= 0) {
            return;
        }

        List<BlockPos> positions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    check(world, positions, pos, x, y, z);
                }
            }
        }

        for (BlockPos p : positions) {
            if (counter.get() <= 0) {
                return;
            }

            destroy(world, player, p, heldItem);
            counter.getAndDecrement();

            destroyConnectedLogs(world, player, p, heldItem, counter);
        }
    }

    private static void check(World world, List<BlockPos> positions, BlockPos pos, int x, int y, int z) {
        if (isLog(world, pos.add(x, y, z))) {
            positions.add(pos.add(x, y, z));
        }
    }

    private static boolean isLog(World world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Config.getLogTypes().stream().anyMatch(l -> l.equals(b.getBlock()));
    }

    private static boolean isGround(World world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Config.getGroundTypes().stream().anyMatch(l -> l.equals(b.getBlock()));
    }

    private static void destroy(World world, PlayerEntity player, BlockPos pos, ItemStack heldItem) {
        if (heldItem != null) {
            heldItem.getItem().onBlockDestroyed(heldItem, world, world.getBlockState(pos), pos, player);
            world.destroyBlock(pos, true);
        }
    }

}
