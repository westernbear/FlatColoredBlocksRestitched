package mod.flatcoloredblocks.gametest;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.client.GuiColoredBlockCrafter;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.MixinEnvironment;

public final class FlatColoredBlocksClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
				.create()) {
			MixinEnvironment.getCurrentEnvironment().audit();
			BlockFlatColored normalBlock = FlatColoredBlocks.first(EnumFlatBlockType.NORMAL);
			BlockFlatColored transparentBlock = FlatColoredBlocks.first(EnumFlatBlockType.TRANSPARENT);
			BlockFlatColored glowingBlock = FlatColoredBlocks.first(EnumFlatBlockType.GLOWING);
			BlockPos normal = context.computeOnClient(
					client -> client.player.blockPosition().east(2).above(2));
			BlockPos transparent = normal.south(2);
			BlockPos glowing = normal.south(4);

			singleplayer.getServer().runCommand(command(
					"setblock %s flatcoloredblocks:flatcoloredblock[shade=645]", normal));
			singleplayer.getServer().runCommand(command(
					"setblock %s flatcoloredblocks:flatcoloredblock_transparent_127[shade=1289]", transparent));
			singleplayer.getServer().runCommand(command(
					"setblock %s flatcoloredblocks:flatcoloredblock_glowing_255[shade=0]", glowing));
			singleplayer
					.getServer()
					.runCommand("item replace entity @a[limit=1] hotbar.0 with chiselsandbits:chisel_diamond");

			context.waitFor(client -> client.level.getBlockState(normal).is(normalBlock)
					&& client.level.getBlockState(transparent).is(transparentBlock)
					&& client.level.getBlockState(glowing).is(glowingBlock)
					&& client.player.getMainHandItem().is(ModItems.ITEM_CHISEL_DIAMOND.get()));
			context.runOnClient(client -> {
				int[] shades = {645, 1289, 0};
				BlockFlatColored[] blocks = {normalBlock, transparentBlock, glowingBlock};
				BlockPos[] positions = {normal, transparent, glowing};
				for (int index = 0; index < positions.length; index++) {
					var state = client.level.getBlockState(positions[index]);
					if (state.getValue(blocks[index].shadeProperty()) != shades[index]) {
						throw new AssertionError("client shade mismatch");
					}
					var tints = client.getBlockColors().getTintSources(state);
					int expectedColor = blocks[index].colorFromState(state);
					if (tints.size() != 1 || tints.getFirst().color(state) != expectedColor) {
						throw new AssertionError("client block tint mismatch");
					}
					client.player.getInventory().setItem(
							index + 1, blocks[index].stackForShade(shades[index], 1));
				}
				client.player.getInventory().setSelectedSlot(0);
			});
			context.getInput().lookAt(transparent);
			context.waitTick();
			context.takeScreenshot("flatcoloredblocks_models_and_item_tints");

			var original = normalBlock.defaultBlockState().setValue(normalBlock.shadeProperty(), 645);
			var support = BlockBitInfo.doSupportAnalysis(original);
			if (!support.isSupported()) {
				throw new AssertionError("C&B rejected colored block: " + support.getUnsupportedReason());
			}

			context.getInput().lookAt(normal);
			context.waitTick();
			context.waitFor(client -> client.hitResult instanceof BlockHitResult hit
					&& hit.getBlockPos().equals(normal));
			context.runOnClient(client -> {
				BlockHitResult hit = (BlockHitResult) client.hitResult;
				client.gameMode.startDestroyBlock(normal, hit.getDirection());
			});
			context.waitFor(client -> client.level.getBlockState(normal).is(ModBlocks.CHISELED_BLOCK.get()));
			waitForSingleBitRemoved(context, singleplayer, normal, original);
			context.takeScreenshot("flatcoloredblocks_chiselsandbits");

			singleplayer.getServer().runCommand("clear @a");
			singleplayer.getServer().runCommand(
					"item replace entity @a[limit=1] hotbar.0 with flatcoloredblocks:coloredcraftingitem");
			singleplayer.getServer().runCommand(
					"item replace entity @a[limit=1] hotbar.1 with minecraft:cobblestone 64");
			singleplayer.getServer().runCommand(
					"item replace entity @a[limit=1] hotbar.2 with minecraft:red_dye 64");
			singleplayer.getServer().runCommand(
					"item replace entity @a[limit=1] hotbar.3 with minecraft:black_dye 64");
			context.waitFor(client -> client.player.getInventory().getItem(0).is(FlatColoredBlocks.CRAFTER_ITEM));
			context.runOnClient(client -> {
				client.player.getInventory().setSelectedSlot(0);
				client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
			});
			context.waitForScreen(GuiColoredBlockCrafter.class);
			context.takeScreenshot("flatcoloredblocks_crafter_top");
			context.getInput().scroll(-6);
			context.waitFor(client -> client.player.containerMenu instanceof ContainerColoredBlockCrafter menu
					&& menu.getScrollRow() > 0);
			context.takeScreenshot("flatcoloredblocks_crafter_scrolled");
		}
	}

	private static void waitForSingleBitRemoved(
			ClientGameTestContext context,
			TestSingleplayerContext singleplayer,
			BlockPos target,
			net.minecraft.world.level.block.state.BlockState original) {
		for (int tick = 0; tick < ClientGameTestContext.DEFAULT_TIMEOUT; tick++) {
			boolean ready = singleplayer.getServer().computeOnServer(server -> {
				var blockEntity = server.overworld().getBlockEntity(target);
				return blockEntity instanceof TileEntityBlockChiseled bits
						&& bits.getBlob().filled() == 4095
						&& bits.getPrimaryBlockStateId() == ModUtil.getStateId(original);
			});
			if (ready) {
				return;
			}
			context.waitTick();
		}
		throw new AssertionError("C&B did not preserve the colored shade while chiseling");
	}

	private static String command(String format, BlockPos position) {
		return format.formatted(position.getX() + " " + position.getY() + " " + position.getZ());
	}
}
