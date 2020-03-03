package de.maxhenkel.reap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class Events {

    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) {
            return;
        }

        PlayerEntity player = event.getPlayer();

        if (player == null) {
            return;
        }

        BlockPos clickedBlock = event.getPos();

        boolean success = Harvester.harvest(clickedBlock, player);

        if (success && event.isCancelable()) {
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        IWorld world = event.getWorld();
        PlayerEntity player = event.getPlayer();

        if (event.getWorld().isRemote() || player == null || event.isCanceled() || player.abilities.isCreativeMode) {
            return;
        }

        BlockPos pos = event.getPos();

        TreeHarvester harvester = new TreeHarvester(pos, player, world);

        harvester.harvest();
    }
}
