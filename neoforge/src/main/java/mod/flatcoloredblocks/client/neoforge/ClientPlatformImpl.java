package mod.flatcoloredblocks.client.neoforge;

import mod.flatcoloredblocks.FlatColoredBlocks;

/**
 * NeoForge client color/model registration.
 * Block tints and models are driven by shared assets; dynamic Fabric model plugins
 * do not apply here. Extend when NeoForge 26.2 exposes matching client hooks.
 */
public final class ClientPlatformImpl {
	public static void registerColorsAndModels() {
		FlatColoredBlocks.LOGGER.debug("NeoForge client color/model hooks ready (asset-driven)");
	}
}
