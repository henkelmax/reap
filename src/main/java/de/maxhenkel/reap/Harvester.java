package de.maxhenkel.reap;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.*;

import java.util.List;

public class Harvester {

    public static boolean harvest(BlockPos pos, PlayerEntity player) {
        World world = player.world;

        BlockState state = world.getBlockState(pos);

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

        if (world.isRemote || !(world instanceof ServerWorld)) {
            return true;
        }

        LootContext.Builder context = new LootContext.Builder((ServerWorld) world).withParameter(LootParameters.field_216286_f, pos).withParameter(LootParameters.field_216287_g, state).withParameter(LootParameters.field_216281_a, player).withParameter(LootParameters.field_216289_i, ItemStack.EMPTY);

        List<ItemStack> drops = state.getDrops(context);

        BlockState newState = blockClicked.getDefaultState();

        if (state.getProperties().stream().anyMatch(p -> p.equals(HorizontalBlock.HORIZONTAL_FACING))) {
            newState = newState.with(HorizontalBlock.HORIZONTAL_FACING, state.get(HorizontalBlock.HORIZONTAL_FACING));
        }

        if (state.getProperties().stream().anyMatch(p -> p.equals(CropsBlock.AGE))) {
            newState = state.with(CropsBlock.AGE, 0);
        }

        world.setBlockState(pos, newState);

        for (ItemStack stack : drops) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }

        return true;
    }

}
