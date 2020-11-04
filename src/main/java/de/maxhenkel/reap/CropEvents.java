package de.maxhenkel.reap;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class CropEvents {

    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event) {
        if (harvest(event.getPos(), event.getPlayer())) {
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static boolean harvest(BlockPos pos, PlayerEntity player) {
        World world = player.world;
        BlockState state = world.getBlockState(pos);
        Block blockClicked = state.getBlock();

        if (Main.SERVER_CONFIG.reapWhitelist.stream().noneMatch(tag -> tag.contains(state.getBlock()))) {
            return false;
        }

        IGrowable growble = getGrowable(blockClicked);

        if (growble == null) {
            return false;
        }

        if (growble.canGrow(world, pos, state, world.isRemote)) {
            return false;
        }

        if (world.isRemote || !(world instanceof ServerWorld)) {
            return true;
        }

        LootContext.Builder context = new LootContext.Builder((ServerWorld) world).withParameter(LootParameters.field_237457_g_, new Vector3d(pos.getX(), pos.getY(), pos.getZ())).withParameter(LootParameters.BLOCK_STATE, state).withParameter(LootParameters.THIS_ENTITY, player);

        if (Main.SERVER_CONFIG.considerTool.get()) {
            context.withParameter(LootParameters.TOOL, player.getHeldItemMainhand());
        } else {
            context.withParameter(LootParameters.TOOL, ItemStack.EMPTY);
        }

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

    private static IGrowable getGrowable(Block block) {
        if (block instanceof IGrowable) {
            return (IGrowable) block;
        }

        if (block instanceof NetherWartBlock) {
            return new IGrowable() {
                @Override
                public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
                    return state.get(NetherWartBlock.AGE) < 3;
                }

                @Override
                public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
                    return false;
                }

                @Override
                public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
                }
            };
        }
        return null;
    }

}
