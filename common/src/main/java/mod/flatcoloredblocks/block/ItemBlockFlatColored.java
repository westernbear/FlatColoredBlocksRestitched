package mod.flatcoloredblocks.block;

import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import mod.flatcoloredblocks.FlatColoredBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;

public final class ItemBlockFlatColored extends BlockItem {
	public ItemBlockFlatColored(BlockFlatColored block, Item.Properties properties) {
		super(block, properties);
	}

	private BlockFlatColored coloredBlock() {
		return (BlockFlatColored) getBlock();
	}

	@Override
	public ItemStack getDefaultInstance() {
		return coloredBlock().stackForShade(0, 1);
	}

	@Override
	public Component getName(ItemStack stack) {
		BlockFlatColored block = coloredBlock();
		BlockState state = block.stateFromStack(stack);
		Set<EnumFlatColorAttributes> attributes = block.getFlatColorAttributes(state);
		String prefix = attributes.contains(EnumFlatColorAttributes.dark)
				? "flatcoloredblocks.dark"
				: attributes.contains(EnumFlatColorAttributes.light)
						? "flatcoloredblocks.light"
						: "flatcoloredblocks.";
		String hue = attributes.stream()
				.filter(attribute -> !attribute.isModifier)
				.findFirst()
				.orElse(EnumFlatColorAttributes.black)
				.name();

		Component name = Component.translatable(prefix + hue + ".name");
		if (block.getType() == EnumFlatBlockType.TRANSPARENT) {
			name = Component.translatable("flatcoloredblocks.Transparent.name").append(" ").append(name);
		} else if (block.getType() == EnumFlatBlockType.GLOWING) {
			name = Component.translatable("flatcoloredblocks.Glowing.name").append(" ").append(name);
		}
		return name.copy()
				.append(" ")
				.append(Component.translatable("flatcoloredblocks.Shade.name"))
				.append(Integer.toString(block.getShadeNumber(state)));
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> output,
			TooltipFlag flag) {
		BlockFlatColored block = coloredBlock();
		int hsv = block.hsvFromState(block.stateFromStack(stack));
		int rgb = ConversionHSV2RGB.toRGB(hsv);
		int red = rgb >> 16 & 0xff;
		int green = rgb >> 8 & 0xff;
		int blue = rgb & 0xff;

		if (FlatColoredBlocks.CONFIG.showRGB) {
			line(output, "flatcoloredblocks.tooltips.rgb", red + " " + green + " " + blue);
		}
		if (FlatColoredBlocks.CONFIG.showHEX) {
			line(output, "flatcoloredblocks.tooltips.hex", String.format(Locale.ROOT, "#%06X", rgb));
		}
		if (FlatColoredBlocks.CONFIG.showHSV) {
			line(
					output,
					"flatcoloredblocks.tooltips.hsv",
					(360 * (hsv >> 16 & 0xff) / 255)
							+ "° "
							+ (100 * (hsv >> 8 & 0xff) / 255)
							+ "% "
							+ (100 * (hsv & 0xff) / 255)
							+ "%");
		}

		if (FlatColoredBlocks.CONFIG.showLight && block.lightValue > 0) {
			line(output, "flatcoloredblocks.tooltips.lightvalue", block.lightValue + "/15");
		}
		if (FlatColoredBlocks.CONFIG.showOpacity && block.opacity < 100) {
			line(output, "flatcoloredblocks.tooltips.opacity", block.opacity + "%");
		}

		super.appendHoverText(stack, context, display, output, flag);
	}

	private static void line(Consumer<Component> output, String key, String value) {
		output.accept(Component.translatable(key).append(" ").append(value));
	}

	public static String hexPad(String value) {
		return value.length() < 2 ? "0" + value : value;
	}
}
