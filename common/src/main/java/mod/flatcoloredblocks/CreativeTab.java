package mod.flatcoloredblocks;

import java.util.ArrayList;
import java.util.List;
import mod.flatcoloredblocks.block.BlockFlatColored;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

final class CreativeTab {
	private static List<ItemStack> icons;

	private CreativeTab() {
	}

	static ItemStack icon() {
		if (icons == null) {
			icons = new ArrayList<>();
			for (BlockFlatColored block : FlatColoredBlocks.BLOCKS) {
				for (int shade = 0; shade < block.getNumberOfShades(); shade++) {
					int hsv = block.hsvFromState(block.defaultBlockState().setValue(block.shadeProperty(), shade));
					int saturation = hsv >> 8 & 0xff;
					int value = hsv & 0xff;
					if (saturation >= 200 && value >= 140 && value <= 170) {
						icons.add(block.stackForShade(shade, 1));
					}
				}
			}
			if (icons.isEmpty() && !FlatColoredBlocks.BLOCKS.isEmpty()) {
				icons.add(FlatColoredBlocks.BLOCKS.getFirst().stackForShade(0, 1));
			}
		}

		if (icons.isEmpty()) {
			return new ItemStack(Blocks.COBBLESTONE);
		}
		return icons.get((int) ((Util.getMillis() / 700) % icons.size())).copy();
	}
}
