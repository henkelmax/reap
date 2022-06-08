package de.maxhenkel.reap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class CropEvents {

    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event) {
        if (harvest(event.getPos(), event.getPlayer())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static boolean harvest(BlockPos pos, Player player) {
        Level world = player.level;
        BlockState state = world.getBlockState(pos);
        Block blockClicked = state.getBlock();

        if (Main.SERVER_CONFIG.reapWhitelist.stream().noneMatch(tag -> tag.contains(state.getBlock()))) {
            return false;
        }

        BonemealableBlock growble = getGrowable(blockClicked);

        if (growble == null) {
            return false;
        }

        if (growble.isValidBonemealTarget(world, pos, state, world.isClientSide)) {
            return false;
        }

        if (world.isClientSide || !(world instanceof ServerLevel)) {
            return true;
        }

        LootContext.Builder context = new LootContext.Builder((ServerLevel) world).withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ())).withParameter(LootContextParams.BLOCK_STATE, state).withParameter(LootContextParams.THIS_ENTITY, player);

        if (Main.SERVER_CONFIG.considerTool.get()) {
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

        for (ItemStack stack : drops) {
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
                public boolean isValidBonemealTarget(BlockGetter worldIn, BlockPos pos, BlockState state, boolean isClient) {
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
