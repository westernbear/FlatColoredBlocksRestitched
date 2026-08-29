package mod.flatcoloredblocks.api;

import mod.flatcoloredblocks.block.EnumFlatBlockType;

public interface ColorConfigView {
	ColorType type();

	EnumFlatBlockType template();

	int shadeCount();

	int variantCount();

	String textureStyle();

	String blockName(int variant);
}
