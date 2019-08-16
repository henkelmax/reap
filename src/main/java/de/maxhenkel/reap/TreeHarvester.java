package de.maxhenkel.reap;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;
import de.maxhenkel.reap.config.BlockSelector;
import de.maxhenkel.reap.config.ItemStackSelector;
import de.maxhenkel.reap.proxy.CommonProxy;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeHarvester {

    private BlockPos pos;
    private EntityPlayer player;
    private World world;
    private ItemStack heldItem;
    private IBlockState meta;

    public TreeHarvester(BlockPos pos, EntityPlayer player, World world) {
        this.pos = pos;
        this.player = player;
        this.world = world;
        this.heldItem = player.getHeldItemMainhand();
    }

    public void harvest() {

        if (!CommonProxy.enableTreeHarvest) {
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

        if (!ItemStackSelector.contains(CommonProxy.allowedTreeTools, heldItem)) {
            return;
        }

        this.meta = getMeta(pos);

        if (!isLog(pos)) {
            return;
        }

        if (!isGround(pos.down())) {
            return;
        }

        ImmutableMap<IProperty<?>, Comparable<?>> properties = world.getBlockState(pos).getProperties();
        if (properties.get(BlockLog.LOG_AXIS).equals(BlockLog.EnumAxis.Y) || properties.get(BlockLog.LOG_AXIS).equals(BlockLog.EnumAxis.NONE)) {
            destroyTree();
        }
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

    public IBlockState getMeta(BlockPos pos) {
        try {
            return world.getBlockState(pos).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE);
        } catch (Exception e) {
            return world.getBlockState(pos);
        }
    }

    private boolean isLog(BlockPos pos) {
        IBlockState b = world.getBlockState(pos);
        if (BlockSelector.contains(CommonProxy.logTypes, b)) {
            if (getMeta(pos) == this.meta) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean isGround(BlockPos pos) {
        IBlockState b = world.getBlockState(pos);
        if (BlockSelector.contains(CommonProxy.groundTypes, b)) {
            return true;
        } else {
            return false;
        }
    }

    private void destroy(BlockPos pos) {
        if (heldItem != null) {
            heldItem.getItem().onBlockDestroyed(heldItem, world, world.getBlockState(pos), pos, player);
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
