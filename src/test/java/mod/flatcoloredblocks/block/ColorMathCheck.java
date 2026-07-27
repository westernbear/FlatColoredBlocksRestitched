package mod.flatcoloredblocks.block;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mod.flatcoloredblocks.config.ModConfig;

public final class ColorMathCheck {
	public static void main(String[] args) throws Exception {
		Path directory = Files.createTempDirectory("flatcoloredblocks-color-math");
		Path configPath = directory.resolve("flatcoloredblocks.properties");
		try {
			ModConfig config = new ModConfig(configPath);
			for (EnumFlatBlockType type : EnumFlatBlockType.values()) {
				BlockHSVConfiguration colors = new BlockHSVConfiguration(type, config);
				if (colors.MAX_SHADES != 1290
						|| ConversionHSV2RGB.toRGB(colors.hsvFromNumber(0)) != 3352616
						|| ConversionHSV2RGB.toRGB(colors.hsvFromNumber(1289)) != 0xffffff) {
					throw new AssertionError("The legacy 1,290-shade palette changed for " + type);
				}
			}

			Files.writeString(
					configPath,
					"showRGB=false\n"
							+ "solidCraftingOutput=99\n"
							+ "transparentCraftingBlock=not an identifier\n"
							+ "DISPLAY_TEXTURE=stone\n"
							+ "TRANSPARENCY_SHADES=2\n"
							+ "TRANSPARENCY_MIN=0.25\n"
							+ "TRANSPARENCY_MAX=0.75\n",
					StandardCharsets.UTF_8);
			ModConfig loaded = new ModConfig(configPath);
			if (loaded.showRGB
					|| loaded.solidCraftingOutput != 64
					|| !loaded.transparentCraftingBlock.equals("flatcoloredblocks:transparent_crafting_block")
					|| loaded.DISPLAY_TEXTURE != mod.flatcoloredblocks.config.EnumFlatBlockTextures.STONE
					|| loaded.TRANSPARENCY_SHADES != 2
					|| Files.exists(configPath.resolveSibling(configPath.getFileName() + ".tmp"))) {
				throw new AssertionError("Config loading, validation, or atomic saving changed");
			}
		} finally {
			Files.deleteIfExists(configPath);
			Files.deleteIfExists(directory);
		}
	}
}
