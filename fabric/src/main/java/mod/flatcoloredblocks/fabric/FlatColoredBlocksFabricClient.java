package mod.flatcoloredblocks.fabric;

import mod.flatcoloredblocks.client.ClientInit;
import net.fabricmc.api.ClientModInitializer;

public final class FlatColoredBlocksFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientInit.init();
	}
}
