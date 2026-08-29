package mod.flatcoloredblocks.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockFlatColored;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ExportFCBlockList {
	private static final String FILE_NAME = "flatcoloredblocks.csv";

	private ExportFCBlockList() {
	}

	public static void register() {
		CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("flatcoloredblock_export_list")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(context -> execute(context.getSource()))));
	}

	private static int execute(CommandSourceStack source) {
		Path output = FlatColoredBlocks.CONFIG.getFilePath().resolveSibling(FILE_NAME);
		try {
			writeCsv(output);
			source.sendSuccess(
					() -> Component.translatable(
							"flatcoloredblocks.commands.export_list.savedfile",
							output.toAbsolutePath()),
					false);
			return 1;
		} catch (IOException exception) {
			FlatColoredBlocks.LOGGER.error("Unable to export {}", output, exception);
			source.sendFailure(Component.translatable(
					"flatcoloredblocks.commands.export_list.unabletosave",
					output.toAbsolutePath()));
			return 0;
		}
	}

	public static void writeCsv(Path output) throws IOException {
		Path parent = output.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Path temporary = output.resolveSibling(output.getFileName() + ".tmp");
		try {
			try (BufferedWriter writer = Files.newBufferedWriter(
					temporary,
					StandardCharsets.UTF_8)) {
				writeRow(
						writer,
						"Registry ID",
						"Variant",
						"Shade Number",
						"Name",
						"HEX",
						"Red",
						"Green",
						"Blue",
						"Hue",
						"Saturation",
						"Value",
						"Opacity",
						"Light Value");
				for (BlockFlatColored block : FlatColoredBlocks.BLOCKS) {
					String registryId = BuiltInRegistries.BLOCK.getKey(block).toString();
					for (int shade = 0; shade < block.getNumberOfShades(); shade++) {
						BlockState state = block.defaultBlockState()
								.setValue(block.shadeProperty(), shade);
						ItemStack stack = block.stackForState(state, 1);
						int hsv = block.hsvFromState(state);
						int rgb = block.colorFromShade(shade);
						writeRow(
								writer,
								registryId,
								Integer.toString(block.getVariant()),
								Integer.toString(shade),
								stack.getHoverName().getString(),
								String.format(Locale.ROOT, "#%06X", rgb),
								Integer.toString(rgb >> 16 & 0xff),
								Integer.toString(rgb >> 8 & 0xff),
								Integer.toString(rgb & 0xff),
								Integer.toString(360 * (hsv >> 16 & 0xff) / 255),
								Integer.toString(100 * (hsv >> 8 & 0xff) / 255),
								Integer.toString(100 * (hsv & 0xff) / 255),
								Integer.toString(block.opacity),
								Integer.toString(block.lightValue));
					}
				}
			}
			moveAtomically(temporary, output);
		} catch (IOException exception) {
			Files.deleteIfExists(temporary);
			throw exception;
		}
	}

	private static void moveAtomically(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException ignored) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void writeRow(BufferedWriter writer, String... values) throws IOException {
		for (int index = 0; index < values.length; index++) {
			if (index > 0) {
				writer.write(',');
			}
			writer.write('"');
			writer.write(values[index].replace("\"", "\"\""));
			writer.write('"');
		}
		writer.newLine();
	}
}
