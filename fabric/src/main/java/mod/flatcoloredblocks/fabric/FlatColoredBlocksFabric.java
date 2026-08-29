package mod.flatcoloredblocks.fabric;

import mod.flatcoloredblocks.FlatColoredBlocks;
import net.fabricmc.api.ModInitializer;

public final class FlatColoredBlocksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		FlatColoredBlocks.init();
	}
}
