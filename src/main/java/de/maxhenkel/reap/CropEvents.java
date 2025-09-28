package de.maxhenkel.reap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class CropEvents {

    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event) {
        if (harvest(event.getHitVec(), event.getPos(), event.getEntity())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static boolean harvest(BlockHitResult hitResult, BlockPos pos, Player player) {
        Level world = player.level();
        BlockState state = world.getBlockState(pos);
        Block blockClicked = state.getBlock();

        if (ReapMod.SERVER_CONFIG.reapWhitelist.stream().noneMatch(tag -> tag.contains(state.getBlock()))) {
            return false;
        }

        BonemealableBlock growable = getGrowable(blockClicked);

        if (growable == null) {
            return false;
        }

        if (growable.isValidBonemealTarget(world, pos, state)) {
            return false;
        }

        if (world.isClientSide() || !(world instanceof ServerLevel)) {
            return true;
        }

        LootParams.Builder context = new LootParams.Builder((ServerLevel) world).withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ())).withParameter(LootContextParams.BLOCK_STATE, state).withParameter(LootContextParams.THIS_ENTITY, player);

        if (ReapMod.SERVER_CONFIG.considerTool.get()) {
            context.withParameter(LootContextParams.TOOL, player.getMainHandItem());
        } else {
            context.withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        }

        List<ItemStack> drops = state.getDrops(context);

        BlockState newState = blockClicked.defaultBlockState();

        if (state.getProperties().stream().anyMatch(p -> p.equals(BlockStateProperties.HORIZONTAL_FACING))) {
            newState = newState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        if (state.getProperties().stream().anyMatch(p -> p.equals(BlockStateProperties.AGE_7))) {
            newState = state.setValue(BlockStateProperties.AGE_7, 0);
        }

        world.setBlockAndUpdate(pos, newState);

        ItemStack clickedBlockItem = state.getCloneItemStack(pos, world, true, player);
        for (ItemStack stack : drops) {
            if (stack.is(clickedBlockItem.getItem())) {
                stack.shrink(1);
            }
            if (stack.isEmpty()) {
                continue;
            }
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }

        return true;
    }

    private static BonemealableBlock getGrowable(Block block) {
        if (block instanceof BonemealableBlock) {
            return (BonemealableBlock) block;
        }

        if (block instanceof NetherWartBlock) {
            return new BonemealableBlock() {

                @Override
                public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos pos, BlockState state) {
                    return state.getValue(NetherWartBlock.AGE) < 3;
                }

                @Override
                public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
                    return false;
                }

                @Override
                public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {

                }
            };
        }
        return null;
    }

}
