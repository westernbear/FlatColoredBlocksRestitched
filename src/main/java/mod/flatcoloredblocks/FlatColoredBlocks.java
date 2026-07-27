package mod.flatcoloredblocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.BlockHSVConfiguration;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.block.ItemBlockFlatColored;
import mod.flatcoloredblocks.commands.ExportFCBlockList;
import mod.flatcoloredblocks.config.ModConfig;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import mod.flatcoloredblocks.craftingitem.CraftingSettings;
import mod.flatcoloredblocks.craftingitem.ItemColoredBlockCrafter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlatColoredBlocks implements ModInitializer {
	public static final String MOD_ID = "flatcoloredblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger("FlatColoredBlocks");
	public static final List<BlockFlatColored> BLOCKS = new ArrayList<>();
	public static final Map<EnumFlatBlockType, BlockHSVConfiguration> CONFIGURATIONS =
			new EnumMap<>(EnumFlatBlockType.class);

	public static FlatColoredBlocks instance;
	public static ModConfig CONFIG;
	public static CraftingSettings CRAFTING_SETTINGS;
	public static MenuType<ContainerColoredBlockCrafter> CRAFTER_MENU;
	public static ItemColoredBlockCrafter CRAFTER_ITEM;
	public static CreativeModeTab CREATIVE_TAB;

	@Override
	public void onInitialize() {
		instance = this;
		CONFIG = new ModConfig(FabricLoader.getInstance().getConfigDir().resolve("flatcoloredblocks.properties"));
		CRAFTING_SETTINGS = CraftingSettings.from(CONFIG);

		for (EnumFlatBlockType type : EnumFlatBlockType.values()) {
			BlockHSVConfiguration configuration = new BlockHSVConfiguration(type, CONFIG);
			CONFIGURATIONS.put(type, configuration);
			for (int variant = 0; variant < configuration.MAX_SHADE_VARIANT; variant++) {
				register(configuration, variant);
			}
		}
		BLOCKS.forEach(block -> BlockBitInfo.forceStateCompatibility(block, true));

		CRAFTER_MENU = Registry.register(
				BuiltInRegistries.MENU,
				id("colored_block_crafter"),
				new ExtendedMenuType<>(
						ContainerColoredBlockCrafter::new,
						CraftingSettings.STREAM_CODEC));

		ResourceKey<Item> crafterKey = ResourceKey.create(Registries.ITEM, id("coloredcraftingitem"));
		CRAFTER_ITEM = Registry.register(
				BuiltInRegistries.ITEM,
				crafterKey,
				new ItemColoredBlockCrafter(new Item.Properties().setId(crafterKey)));

		CREATIVE_TAB = Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				id("main"),
				CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
						.title(Component.translatable("itemGroup.FlatColoredBlocks"))
						.icon(CreativeTab::icon)
						.displayItems((parameters, output) -> {
							output.accept(CRAFTER_ITEM);
							BLOCKS.forEach(block -> block.addAllShades(output::accept));
						})
						.build());

		ExportFCBlockList.register();
		LOGGER.info(
				"Registered {} blocks and {} shades",
				BLOCKS.size(),
				getFullNumberOfShades());
	}

	private static void register(BlockHSVConfiguration configuration, int variant) {
		Identifier id = id(configuration.getBlockName(variant));
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		BlockFlatColored block = BlockFlatColored.construct(configuration, variant, blockKey);
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		ItemBlockFlatColored item = new ItemBlockFlatColored(
				block,
				block.configureItemProperties(new Item.Properties().setId(itemKey)));
		item.registerBlocks(Item.BY_BLOCK, item);
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		BLOCKS.add(block);
	}

	public static int getFullNumberOfShades() {
		return BLOCKS.stream().mapToInt(BlockFlatColored::getNumberOfShades).sum();
	}

	public static int getFullNumberOfBlocks() {
		return BLOCKS.size();
	}

	public static BlockFlatColored first(EnumFlatBlockType type) {
		return BLOCKS.stream()
				.filter(block -> block.getType() == type)
				.findFirst()
				.orElseThrow();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
