package de.maxhenkel.reap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Events {

	@SubscribeEvent
	public void onPlayerUse(PlayerInteractEvent.RightClickBlock event) {
		if (event.isCanceled()) {
			return;
		}

		EntityPlayer player = event.getEntityPlayer();

		if (player == null) {
			return;
		}

		BlockPos clickedBlock = event.getPos();

		boolean success = Harvester.harvest(clickedBlock, player);

		if (success && event.isCancelable()) {
			event.setResult(Result.DENY);
			event.setCanceled(true);
		}

	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		World world = event.getWorld();
		EntityPlayer player = event.getPlayer();

		if (event.getWorld().isRemote || player == null || event.isCanceled() || player.capabilities.isCreativeMode) {
			return;
		}

		BlockPos pos = event.getPos();

		TreeHarvester harvester = new TreeHarvester(pos, player, world);

		harvester.harvest();
	}
}
