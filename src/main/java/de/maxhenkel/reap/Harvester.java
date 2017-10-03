package de.maxhenkel.reap;

import java.util.List;
import de.maxhenkel.reap.config.BlockSelector;
import de.maxhenkel.reap.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Harvester {

	public static boolean harvest(BlockPos pos, EntityPlayer player) {
		World world = player.world;

		IBlockState state = world.getBlockState(pos);

		Block blockClicked = state.getBlock();

		if (!BlockSelector.contains(Config.reapWhitelist, state)) {
			return false;
		}

		if (!(blockClicked instanceof IGrowable)) {
			return false;
		}

		IGrowable growble = (IGrowable) blockClicked;

		if (growble.canGrow(world, pos, state, world.isRemote)) {
			return false;
		}
		
		if(world.isRemote){
			return true;
		}

		List<ItemStack> drops = blockClicked.getDrops(world, pos, state, 0);

		IBlockState newState = blockClicked.getDefaultState();

		if (state.getProperties().containsKey(BlockHorizontal.FACING)) {
			newState = newState.withProperty(BlockHorizontal.FACING, state.getValue(BlockHorizontal.FACING));
		}

		if (state.getProperties().containsKey(BlockCrops.AGE)) {
			newState=state.withProperty(BlockCrops.AGE, 0);
		}

		world.setBlockState(pos, newState);

		for (ItemStack stack : drops) {
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
		}

		return true;
	}

	public static boolean contains(Block[] blocks, Block b) {
		for (Block block : blocks) {
			if (block.equals(b)) {
				return true;
			}
		}

		return false;
	}

}
