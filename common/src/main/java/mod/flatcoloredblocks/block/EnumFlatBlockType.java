package mod.flatcoloredblocks.block;

import mod.flatcoloredblocks.FlatColoredBlocks;

public enum EnumFlatBlockType {
	NORMAL("flatcoloredblock"),
	TRANSPARENT("flatcoloredblock_transparent"),
	GLOWING("flatcoloredblock_glowing");

	public final String blockName;

	EnumFlatBlockType(String blockName) {
		this.blockName = blockName;
	}

	public int getOutputCount() {
		if (FlatColoredBlocks.CONFIG == null) {
			return 1;
		}
		return switch (this) {
			case NORMAL -> FlatColoredBlocks.CONFIG.solidCraftingOutput;
			case TRANSPARENT -> FlatColoredBlocks.CONFIG.transparentCraftingOutput;
			case GLOWING -> FlatColoredBlocks.CONFIG.glowingCraftingOutput;
		};
	}

	public boolean translucent() {
		return this == TRANSPARENT;
	}

	public String defaultRegistryName() {
		return switch (this) {
			case NORMAL -> blockName;
			case TRANSPARENT -> blockName + "_127";
			case GLOWING -> blockName + "_255";
		};
	}
}
