package mod.flatcoloredblocks.client;

import java.util.List;
import java.util.Set;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockFlatColored;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class ClientSide implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerColors();
		registerModels();
		MenuScreens.register(FlatColoredBlocks.CRAFTER_MENU, GuiColoredBlockCrafter::new);
	}

	private static void registerColors() {
		for (BlockFlatColored block : FlatColoredBlocks.BLOCKS) {
			BlockColorRegistry.register(List.of(new BlockTintSource() {
				@Override
				public int color(BlockState state) {
					return block.colorFromState(state);
				}

				@Override
				public Set<Property<?>> relevantProperties() {
					return Set.of(block.shadeProperty());
				}
			}), block);
		}
	}

	private static void registerModels() {
		ModelLoadingPlugin.register(context -> {
			for (BlockFlatColored block : FlatColoredBlocks.BLOCKS) {
				var model = new SingleVariant.Unbaked(new Variant(
						FlatColoredBlocks.id("block/flatcoloredblock_" + block.textureStyle())))
						.asRoot();
				context.registerBlockStateResolver(block, resolver -> {
					for (BlockState state : block.getStateDefinition().getPossibleStates()) {
						resolver.setModel(state, model);
					}
				});
			}
		});
	}
}
