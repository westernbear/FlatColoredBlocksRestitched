package mod.flatcoloredblocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mod.flatcoloredblocks.api.ColorTypeRegistration;
import mod.flatcoloredblocks.api.FlatColoredBlocksAPI;
import mod.flatcoloredblocks.api.impl.FlatColoredBlocksAPIImpl;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.BlockHSVConfiguration;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.block.ItemBlockFlatColored;
import mod.flatcoloredblocks.commands.ExportFCBlockList;
import mod.flatcoloredblocks.compat.ChiselsAndBitsCompat;
import mod.flatcoloredblocks.config.ModConfig;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import mod.flatcoloredblocks.craftingitem.CraftingSettings;
import mod.flatcoloredblocks.craftingitem.ItemColoredBlockCrafter;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.menu.MenuRegistry;
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

public final class FlatColoredBlocks {
	public static final String MOD_ID = "flatcoloredblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger("FlatColoredBlocks");
	public static final List<BlockFlatColored> BLOCKS = new ArrayList<>();
	public static final Map<EnumFlatBlockType, BlockHSVConfiguration> CONFIGURATIONS =
			new EnumMap<>(EnumFlatBlockType.class);
	public static final Map<Identifier, BlockHSVConfiguration> CUSTOM_CONFIGURATIONS = new LinkedHashMap<>();

	public static FlatColoredBlocks instance;
	public static ModConfig CONFIG;
	public static CraftingSettings CRAFTING_SETTINGS;
	public static MenuType<ContainerColoredBlockCrafter> CRAFTER_MENU;
	public static ItemColoredBlockCrafter CRAFTER_ITEM;
	public static CreativeModeTab CREATIVE_TAB;

	private static boolean registrationOpen = false;
	private static boolean frozen = false;
	private static boolean setupHookRegistered = false;

	private FlatColoredBlocks() {}

	public static void init() {
		instance = new FlatColoredBlocks();
		CONFIG = new ModConfig(Platform.getConfigFolder().resolve("flatcoloredblocks.properties"));
		CRAFTING_SETTINGS = CraftingSettings.from(CONFIG);
		registrationOpen = true;
		frozen = false;

		for (EnumFlatBlockType type : EnumFlatBlockType.values()) {
			BlockHSVConfiguration configuration = new BlockHSVConfiguration(type, CONFIG);
			CONFIGURATIONS.put(type, configuration);
			for (int variant = 0; variant < configuration.MAX_SHADE_VARIANT; variant++) {
				registerBlockVariant(configuration, variant);
			}
		}

		FlatColoredBlocksAPIImpl.bootstrapBuiltins();

		CRAFTER_MENU = Registry.register(
				BuiltInRegistries.MENU,
				id("colored_block_crafter"),
				MenuRegistry.ofExtended((containerId, inventory, buf) ->
						new ContainerColoredBlockCrafter(
								containerId,
								inventory,
								CraftingSettings.STREAM_CODEC.decode(buf))));

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
		registerSetupHook();

		LOGGER.info(
				"Registered {} built-in blocks ({} shades); addon color types accepted until SETUP",
				BLOCKS.size(),
				getFullNumberOfShades());
	}

	private static void registerSetupHook() {
		if (setupHookRegistered) {
			return;
		}
		setupHookRegistered = true;
		LifecycleEvent.SETUP.register(() -> {
			ChiselsAndBitsCompat.applyForcedCompatibility(BLOCKS);
			freezeRegistration();
			FlatColoredBlocksAPIImpl.firePaletteBuilt();
			LOGGER.info(
					"Palette frozen with {} blocks and {} shades",
					BLOCKS.size(),
					getFullNumberOfShades());
		});
	}

	public static boolean isRegistrationOpen() {
		return registrationOpen && !frozen;
	}

	public static void freezeRegistration() {
		frozen = true;
		registrationOpen = false;
	}

	public static BlockFlatColored registerColorTypeInternal(ColorTypeRegistration registration) {
		if (!isRegistrationOpen()) {
			throw new IllegalStateException(
					"Flat Colored Blocks color type registration is closed; register during mod init before SETUP");
		}
		Identifier typeId = registration.id();
		if (CUSTOM_CONFIGURATIONS.containsKey(typeId)) {
			throw new IllegalArgumentException("Duplicate color type id: " + typeId);
		}

		EnumFlatBlockType template = registration.template();
		BlockHSVConfiguration configuration = new BlockHSVConfiguration(template, CONFIG);
		configuration.overrideBlockNamePrefix(registration.blockNamePrefix());
		CUSTOM_CONFIGURATIONS.put(typeId, configuration);

		BlockFlatColored first = null;
		for (int variant = 0; variant < configuration.MAX_SHADE_VARIANT; variant++) {
			BlockFlatColored block = registerBlockVariant(configuration, variant);
			if (first == null) {
				first = block;
			}
			FlatColoredBlocksAPIImpl.fireBlockRegistered(typeId, block);
		}
		return first;
	}

	static BlockFlatColored registerBlockVariant(BlockHSVConfiguration configuration, int variant) {
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
		return block;
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

	public static FlatColoredBlocksAPI api() {
		return FlatColoredBlocksAPI.getInstance();
	}
}
