package de.maxhenkel.reap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Harvester {

    public static boolean harvest(BlockPos pos, EntityPlayer player) {
        World world = player.world;

        IBlockState state = world.getBlockState(pos);

        Block blockClicked = state.getBlock();

        if (Config.reapWhitelist.stream().noneMatch(b -> b.equals(state.getBlock()))) {
            return false;
        }

        if (!(blockClicked instanceof IGrowable)) {
            return false;
        }

        IGrowable growble = (IGrowable) blockClicked;

        if (growble.canGrow(world, pos, state, world.isRemote)) {
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        blockClicked.getDrops(state, drops, world, pos, 0);
        drops.add(new ItemStack(blockClicked.getItemDropped(state, world, pos, 0).asItem()));

        IBlockState newState = blockClicked.getDefaultState();

        if (state.getProperties().stream().anyMatch(p -> p.equals(BlockHorizontal.HORIZONTAL_FACING))) {
            newState = newState.with(BlockHorizontal.HORIZONTAL_FACING, state.get(BlockHorizontal.HORIZONTAL_FACING));
        }

        if (state.getProperties().stream().anyMatch(p -> p.equals(BlockCrops.AGE))) {
            newState = state.with(BlockCrops.AGE, 0);
        }

        world.setBlockState(pos, newState);

        for (ItemStack stack : drops) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }

        return true;
    }

}
