package mod.flatcoloredblocks.block;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import mod.flatcoloredblocks.FlatColoredBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

public final class BlockFlatColored extends Block implements BeaconBeamBlock {
	private static final ThreadLocal<BlockHSVConfiguration> CONSTRUCTION_CONFIG = new ThreadLocal<>();

	private IntegerProperty shade;
	private final BlockHSVConfiguration configuration;
	private final int variantIndex;
	private final int variantValue;
	public final int opacity;
	public final int lightValue;

	public static BlockFlatColored construct(
			BlockHSVConfiguration configuration, int variantIndex, ResourceKey<Block> key) {
		CONSTRUCTION_CONFIG.set(configuration);
		try {
			return new BlockFlatColored(configuration, variantIndex, key);
		} finally {
			CONSTRUCTION_CONFIG.remove();
		}
	}

	private BlockFlatColored(
			BlockHSVConfiguration configuration, int variantIndex, ResourceKey<Block> key) {
		super(properties(configuration, variantIndex, key));
		this.configuration = configuration;
		this.variantIndex = variantIndex;
		this.variantValue = configuration.shadeConvertVariant[variantIndex];
		this.lightValue = lightLevel(configuration, variantIndex);
		this.opacity = configuration.type == EnumFlatBlockType.TRANSPARENT
				? 100 - Math.round(variantValue * 100 / 255.0F)
				: 100;
		registerDefaultState(defaultBlockState().setValue(shade, 0));
	}

	private static BlockBehaviour.Properties properties(
			BlockHSVConfiguration configuration, int variantIndex, ResourceKey<Block> key) {
		boolean translucent = configuration.type == EnumFlatBlockType.TRANSPARENT;
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
				.setId(key)
				.strength(1.5F, 10.0F)
				.lightLevel(state -> lightLevel(configuration, variantIndex))
				.sound(translucent ? SoundType.GLASS : SoundType.STONE)
				.mapColor(MapColor.SNOW)
				.noLootTable();
		return translucent ? properties.noOcclusion() : properties;
	}

	private static int lightLevel(BlockHSVConfiguration configuration, int variantIndex) {
		if (configuration.type != EnumFlatBlockType.GLOWING
				|| !FlatColoredBlocks.CONFIG.GLOWING_EMITS_LIGHT) {
			return 0;
		}
		return Math.clamp(Math.round(15.0F * configuration.shadeConvertVariant[variantIndex] / 255.0F), 0, 15);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		BlockHSVConfiguration configuration = CONSTRUCTION_CONFIG.get();
		if (configuration == null) {
			throw new IllegalStateException("Flat colored block must be constructed through construct()");
		}
		shade = IntegerProperty.create("shade", 0, configuration.MAX_SHADES_MINUS_ONE);
		builder.add(shade);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentState, net.minecraft.core.Direction side) {
		return getType().translucent() && adjacentState.is(this)
				|| super.skipRendering(state, adjacentState, side);
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return getType().translucent() ? 1.0F : super.getShadeBrightness(state, level, pos);
	}

	@Override
	public DyeColor getColor() {
		return DyeColor.WHITE;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		return List.of(stackForState(state, 1));
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return stackForState(state, 1);
	}

	public ItemStack stackForState(BlockState state, int count) {
		return stackForShade(getShadeNumber(state), count);
	}

	public Item.Properties configureItemProperties(Item.Properties properties) {
		return properties
				.component(
						DataComponents.BLOCK_STATE,
						BlockItemStateProperties.EMPTY.with(shade, 0))
				.component(
						DataComponents.ITEM_MODEL,
						FlatColoredBlocks.id("tinted_" + configuration.textureStyle))
				.component(
						DataComponents.CUSTOM_MODEL_DATA,
						new CustomModelData(List.of(), List.of(), List.of(), List.of(colorForTint(0))));
	}

	public ItemStack stackForShade(int shadeNumber, int count) {
		shadeNumber = Math.clamp(shadeNumber, 0, configuration.MAX_SHADES_MINUS_ONE);
		ItemStack stack = new ItemStack(this, count);
		stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(shade, shadeNumber));
		stack.set(DataComponents.ITEM_MODEL, FlatColoredBlocks.id("tinted_" + configuration.textureStyle));
		stack.set(
				DataComponents.CUSTOM_MODEL_DATA,
				new CustomModelData(List.of(), List.of(), List.of(), List.of(colorForTint(shadeNumber))));
		return stack;
	}

	public void addAllShades(Consumer<ItemStack> output) {
		addAllShades(1, output);
	}

	public void addAllShades(int count, Consumer<ItemStack> output) {
		for (int shadeNumber = 0; shadeNumber < configuration.MAX_SHADES; shadeNumber++) {
			output.accept(stackForShade(shadeNumber, count));
		}
	}

	public BlockState stateFromStack(ItemStack stack) {
		return stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
				.apply(defaultBlockState());
	}

	public int colorFromState(BlockState state) {
		return colorForTint(getShadeNumber(state));
	}

	public int colorForTint(int shadeNumber) {
		int alpha = getType() == EnumFlatBlockType.TRANSPARENT ? variantValue : 0xff;
		return alpha << 24 | colorFromShade(shadeNumber);
	}

	public int colorFromShade(int shadeNumber) {
		return ConversionHSV2RGB.toRGB(configuration.hsvFromNumber(shadeNumber));
	}

	public int hsvFromState(BlockState state) {
		return configuration.hsvFromNumber(getShadeNumber(state));
	}

	public int getShadeNumber(BlockState state) {
		if (state.getBlock() instanceof BlockFlatColored block) {
			return state.getValue(block.shade);
		}
		return 0;
	}

	public IntegerProperty shadeProperty() {
		return shade;
	}

	public int getNumberOfShades() {
		return configuration.MAX_SHADES;
	}

	public int getMaxShade() {
		return configuration.MAX_SHADES_MINUS_ONE;
	}

	public EnumFlatBlockType getType() {
		return configuration.type;
	}

	public EnumFlatBlockType getCraftable() {
		return getType();
	}

	public int getVariant() {
		return variantValue;
	}

	public String registryName() {
		return configuration.getBlockName(variantIndex);
	}

	public String textureStyle() {
		return configuration.textureStyle;
	}

	public Set<EnumFlatColorAttributes> getFlatColorAttributes(BlockState state) {
		int hsv = hsvFromState(state);
		int hue = (hsv >> 16 & 0xff) * 360 / 0xff;
		int saturation = hsv >> 8 & 0xff;
		int value = hsv & 0xff;

		if (saturation == 0) {
			if (value < 64) return EnumSet.of(EnumFlatColorAttributes.black);
			if (value > 192) return EnumSet.of(EnumFlatColorAttributes.white);
			if (value > 128) return EnumSet.of(EnumFlatColorAttributes.silver);
			return EnumSet.of(EnumFlatColorAttributes.grey);
		}

		Set<EnumFlatColorAttributes> result = EnumSet.noneOf(EnumFlatColorAttributes.class);
		if (value < 128) {
			result.add(EnumFlatColorAttributes.dark);
		} else if (value > 192 && saturation < 128) {
			result.add(EnumFlatColorAttributes.light);
		}

		if (hue >= 15 && hue <= 45) result.add(EnumFlatColorAttributes.orange);
		else if (hue >= 255 && hue <= 285) result.add(EnumFlatColorAttributes.violet);
		else if (hue >= 315 && hue <= 345) result.add(EnumFlatColorAttributes.pink);
		else if (hue >= 60 && hue <= 90) result.add(EnumFlatColorAttributes.lime);
		else if (hue >= 195 && hue <= 225) result.add(EnumFlatColorAttributes.azure);
		else if (hue >= 125 && hue <= 155) result.add(EnumFlatColorAttributes.emerald);
		else if (hue >= 330 || hue <= 30) result.add(EnumFlatColorAttributes.red);
		else if (hue <= 90) result.add(EnumFlatColorAttributes.yellow);
		else if (hue <= 150) result.add(EnumFlatColorAttributes.green);
		else if (hue <= 210) result.add(EnumFlatColorAttributes.cyan);
		else if (hue <= 270) result.add(EnumFlatColorAttributes.blue);
		else result.add(EnumFlatColorAttributes.magenta);

		return result;
	}
}
