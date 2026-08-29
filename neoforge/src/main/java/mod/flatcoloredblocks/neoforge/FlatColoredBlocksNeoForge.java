package mod.flatcoloredblocks.neoforge;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.client.ClientInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(FlatColoredBlocks.MOD_ID)
public final class FlatColoredBlocksNeoForge {
	public FlatColoredBlocksNeoForge(IEventBus modEventBus) {
		FlatColoredBlocks.init();
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			ClientInit.init();
		}
	}
}
