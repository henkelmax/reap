package de.maxhenkel.reap;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class TreeHarvester {

    private BlockPos pos;
    private PlayerEntity player;
    private IWorld world;
    private ItemStack heldItem;

    public TreeHarvester(BlockPos pos, PlayerEntity player, IWorld world) {
        this.pos = pos;
        this.player = player;
        this.world = world;
        this.heldItem = player.getHeldItemMainhand();
    }

    public void harvest() {

        if (!Config.treeHarvest) {
            return;
        }

        if (heldItem == null) {
            return;
        }

        Item item = heldItem.getItem();

        if (item == null) {
            return;
        }

        if (player.isSneaking()) {
            return;
        }

        if (Config.allowedTreeTools.stream().noneMatch(i -> i.equals(heldItem.getItem()))) {
            return;
        }

        if (!isLog(pos)) {
            return;
        }

        if (!isGround(pos.down())) {
            return;
        }

        if (!world.getBlockState(pos).get(LogBlock.AXIS).equals(Direction.Axis.Y)) {
            return;
        }

        destroyTree();

    }

    private void destroyTree() {
        destroyConnectedLogs(pos, new Counter(128));
    }

    private void destroyConnectedLogs(BlockPos pos, Counter counter) {
        if (counter.isZero()) {
            return;
        }

        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    check(positions, pos, x, y, z);
                }
            }
        }

        for (BlockPos p : positions) {
            if (counter.isZero()) {
                return;
            }

            destroy(p);
            counter.decrement();

            destroyConnectedLogs(p, counter);
        }

    }

    private void check(ArrayList<BlockPos> positions, BlockPos pos, int x, int y, int z) {
        if (isLog(pos.add(x, y, z))) {
            positions.add(pos.add(x, y, z));
        }
    }

    private boolean isLog(BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Config.logTypes.stream().anyMatch(l -> l.equals(b.getBlock()));
    }

    private boolean isGround(BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return Config.groundTypes.stream().anyMatch(l -> l.equals(b.getBlock()));
    }

    private void destroy(BlockPos pos) {
        if (heldItem != null) {
            heldItem.getItem().onBlockDestroyed(heldItem, (World) world, world.getBlockState(pos), pos, player);
            world.destroyBlock(pos, true);
        }
    }

    private class Counter {
        private int i;

        public Counter(int i) {
            this.i = i;
        }

        public boolean isZero() {
            if (this.i <= 0) {
                return true;
            } else {
                return false;
            }
        }

        public Counter decrement() {
            if (this.i <= 0) {
                return this;
            }
            this.i = this.i - 1;
            return this;
        }

    }

}
