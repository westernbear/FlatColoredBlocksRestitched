package mod.flatcoloredblocks.compat;

import java.util.Collection;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.flatcoloredblocks.block.BlockFlatColored;
import dev.architectury.platform.Platform;

/**
 * Soft Chisels & Bits integration. No-ops when the mod is absent.
 * C&B is a compileOnly dependency of common; call sites must check {@link #isLoaded()} first.
 */
public final class ChiselsAndBitsCompat {
	public static final String MOD_ID = "chiselsandbits";

	private ChiselsAndBitsCompat() {}

	public static boolean isLoaded() {
		return Platform.isModLoaded(MOD_ID);
	}

	public static void applyForcedCompatibility(Collection<BlockFlatColored> blocks) {
		if (!isLoaded() || blocks == null || blocks.isEmpty()) {
			return;
		}
		for (BlockFlatColored block : blocks) {
			if (block != null) {
				BlockBitInfo.forceStateCompatibility(block, true);
			}
		}
	}
}
