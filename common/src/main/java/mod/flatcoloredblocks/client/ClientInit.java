package mod.flatcoloredblocks.client;

import mod.flatcoloredblocks.FlatColoredBlocks;
import dev.architectury.registry.client.gui.MenuScreenRegistry;

public final class ClientInit {
	private ClientInit() {}

	public static void init() {
		MenuScreenRegistry.registerScreenFactory(FlatColoredBlocks.CRAFTER_MENU, GuiColoredBlockCrafter::new);
		ClientPlatform.registerColorsAndModels();
	}
}
