package mod.flatcoloredblocks.api;

import mod.flatcoloredblocks.block.BlockFlatColored;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface BlockRegisteredCallback {
	void onBlockRegistered(Identifier colorTypeId, BlockFlatColored block);
}
