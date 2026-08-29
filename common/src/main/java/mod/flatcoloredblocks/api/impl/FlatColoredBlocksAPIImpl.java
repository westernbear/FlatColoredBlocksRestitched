package mod.flatcoloredblocks.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.api.BlockRegisteredCallback;
import mod.flatcoloredblocks.api.ColorConfigView;
import mod.flatcoloredblocks.api.ColorType;
import mod.flatcoloredblocks.api.ColorTypeRegistration;
import mod.flatcoloredblocks.api.CrafterOpenedCallback;
import mod.flatcoloredblocks.api.FlatColoredBlocksAPI;
import mod.flatcoloredblocks.api.PaletteBuiltCallback;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.BlockHSVConfiguration;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class FlatColoredBlocksAPIImpl implements FlatColoredBlocksAPI {
	public static final FlatColoredBlocksAPIImpl INSTANCE = new FlatColoredBlocksAPIImpl();

	private final Map<Identifier, ColorType> types = new LinkedHashMap<>();
	private final List<BlockRegisteredCallback> blockListeners = new CopyOnWriteArrayList<>();
	private final List<PaletteBuiltCallback> paletteListeners = new CopyOnWriteArrayList<>();
	private final List<CrafterOpenedCallback> crafterListeners = new CopyOnWriteArrayList<>();

	private FlatColoredBlocksAPIImpl() {}

	public static void bootstrapBuiltins() {
		for (EnumFlatBlockType type : EnumFlatBlockType.values()) {
			ColorType colorType = ColorType.ofBuiltin(type);
			INSTANCE.types.put(colorType.id(), colorType);
		}
	}

	public static void fireBlockRegistered(Identifier colorTypeId, BlockFlatColored block) {
		for (BlockRegisteredCallback callback : INSTANCE.blockListeners) {
			callback.onBlockRegistered(colorTypeId, block);
		}
	}

	public static void firePaletteBuilt() {
		for (PaletteBuiltCallback callback : INSTANCE.paletteListeners) {
			callback.onPaletteBuilt(INSTANCE);
		}
	}

	public static void fireCrafterOpened(Player player) {
		for (CrafterOpenedCallback callback : INSTANCE.crafterListeners) {
			callback.onCrafterOpened(player);
		}
	}

	@Override
	public String modId() {
		return FlatColoredBlocks.MOD_ID;
	}

	@Override
	public Collection<ColorType> colorTypes() {
		return Collections.unmodifiableCollection(types.values());
	}

	@Override
	public Optional<ColorType> colorType(Identifier id) {
		return Optional.ofNullable(types.get(id));
	}

	@Override
	public Optional<ColorConfigView> configuration(ColorType type) {
		if (type.isBuiltin()) {
			EnumFlatBlockType builtin = type.builtinType().orElseThrow();
			BlockHSVConfiguration configuration = FlatColoredBlocks.CONFIGURATIONS.get(builtin);
			return Optional.ofNullable(configuration).map(config -> view(type, config));
		}
		BlockHSVConfiguration configuration = FlatColoredBlocks.CUSTOM_CONFIGURATIONS.get(type.id());
		return Optional.ofNullable(configuration).map(config -> view(type, config));
	}

	private static ColorConfigView view(ColorType type, BlockHSVConfiguration configuration) {
		return new ColorConfigView() {
			@Override
			public ColorType type() {
				return type;
			}

			@Override
			public EnumFlatBlockType template() {
				return configuration.type;
			}

			@Override
			public int shadeCount() {
				return configuration.getNumberOfShades();
			}

			@Override
			public int variantCount() {
				return configuration.MAX_SHADE_VARIANT;
			}

			@Override
			public String textureStyle() {
				return configuration.textureStyle;
			}

			@Override
			public String blockName(int variant) {
				return configuration.getBlockName(variant);
			}
		};
	}

	@Override
	public Collection<BlockFlatColored> blocks() {
		return Collections.unmodifiableList(FlatColoredBlocks.BLOCKS);
	}

	@Override
	public Optional<BlockFlatColored> firstBlock(ColorType type) {
		if (type.isBuiltin()) {
			return Optional.of(FlatColoredBlocks.first(type.builtinType().orElseThrow()));
		}
		BlockHSVConfiguration configuration = FlatColoredBlocks.CUSTOM_CONFIGURATIONS.get(type.id());
		if (configuration == null) {
			return Optional.empty();
		}
		String prefix = configuration.getBlockName(0);
		return FlatColoredBlocks.BLOCKS.stream()
				.filter(block -> block.registryName().equals(prefix)
						|| block.registryName().startsWith(prefix + "_")
						|| block.registryName().equals(configuration.getBlockName(0)))
				.findFirst();
	}

	@Override
	public int colorFromState(BlockState state) {
		if (state.getBlock() instanceof BlockFlatColored block) {
			return block.colorFromState(state);
		}
		return 0xffffff;
	}

	@Override
	public int shadeToRgb(BlockFlatColored block, int shade) {
		return block.colorFromShade(shade);
	}

	@Override
	public ColorType registerColorType(ColorTypeRegistration registration) {
		ColorType colorType = ColorType.ofCustom(registration.id());
		types.put(colorType.id(), colorType);
		FlatColoredBlocks.registerColorTypeInternal(registration);
		return colorType;
	}

	@Override
	public void addBlockRegisteredListener(BlockRegisteredCallback callback) {
		blockListeners.add(callback);
	}

	@Override
	public void addPaletteBuiltListener(PaletteBuiltCallback callback) {
		paletteListeners.add(callback);
	}

	@Override
	public void addCrafterOpenedListener(CrafterOpenedCallback callback) {
		crafterListeners.add(callback);
	}

	@Override
	public EnumFlatBlockType asBuiltinEnum(ColorType type) {
		return type.builtinType().orElse(EnumFlatBlockType.NORMAL);
	}
}
