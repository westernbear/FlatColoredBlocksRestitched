package mod.flatcoloredblocks.gametest;

import com.mojang.serialization.Dynamic;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.legacy.LegacyChiseledBlockFix;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModTags;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.block.EnumFlatColorAttributes;
import mod.flatcoloredblocks.block.ItemBlockFlatColored;
import mod.flatcoloredblocks.commands.ExportFCBlockList;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import mod.flatcoloredblocks.datafixer.LegacyFCBBlockFix;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class FlatColoredBlocksGameTests {
	private static final int DATA_VERSION_1_12_2 = 1343;

	@GameTest(maxTicks = 200)
	public void coreFeatures(GameTestHelper helper) {
		helper.assertValueEqual(FlatColoredBlocks.BLOCKS.size(), 3, "registered colored blocks");
		var player = helper.makeMockPlayer(GameType.CREATIVE);

		for (int blockIndex = 0; blockIndex < EnumFlatBlockType.values().length; blockIndex++) {
			EnumFlatBlockType type = EnumFlatBlockType.values()[blockIndex];
			BlockFlatColored block = FlatColoredBlocks.first(type);
			Block registered = BuiltInRegistries.BLOCK.getValue(FlatColoredBlocks.id(block.registryName()));
			helper.assertTrue(registered == block, "wrong block registry value for " + block.registryName());
			helper.assertTrue(Item.byBlock(block) instanceof ItemBlockFlatColored, "missing block item");
			helper.assertValueEqual(block.getNumberOfShades(), 1290, "legacy shade count");
			helper.assertValueEqual(
					block.getStateDefinition().getPossibleStates().size(),
					block.getNumberOfShades(),
					"shade state count for " + block.registryName());

			var creativeStacks = new ArrayList<ItemStack>();
			block.addAllShades(creativeStacks::add);
			helper.assertValueEqual(
					creativeStacks.size(),
					block.getNumberOfShades(),
					"creative shade count for " + block.registryName());
			assertStack(helper, block, creativeStacks.getFirst(), 0, "first creative stack");
			assertStack(helper, block, creativeStacks.getLast(), block.getMaxShade(), "last creative stack");

			BlockState defaultState = block.defaultBlockState();
			helper.assertValueEqual(defaultState.getLightEmission(), block.lightValue, "light level");
			helper.assertValueEqual(defaultState.canOcclude(), !type.translucent(), "occlusion");
			if (type.translucent()) {
				helper.assertTrue(defaultState.skipRendering(defaultState, Direction.NORTH), "internal glass face");
			}

			int[] shades = {0, block.getMaxShade() / 2, block.getMaxShade()};
			for (int shadeIndex = 0; shadeIndex < shades.length; shadeIndex++) {
				int shade = shades[shadeIndex];
				ItemStack stack = block.stackForShade(shade, 1);
				assertStack(helper, block, stack, shade, "generated stack");

				BlockPos support = new BlockPos(1 + shadeIndex * 2, 1, 1 + blockIndex * 2);
				helper.setBlock(support, Blocks.STONE);
				player.setItemInHand(InteractionHand.MAIN_HAND, stack);
				BlockPos absoluteSupport = helper.absolutePos(support);
				var hit = new BlockHitResult(
						Vec3.atCenterOf(absoluteSupport), Direction.UP, absoluteSupport, false);
				stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

				BlockPos placed = support.above();
				BlockPos absolutePlaced = helper.absolutePos(placed);
				BlockState actual = helper.getBlockState(placed);
				helper.assertTrue(actual.is(block), "BlockItem did not place the expected block");
				helper.assertValueEqual(actual.getValue(block.shadeProperty()), shade, "placed shade");
				helper.assertTrue(actual.is(ModTags.Blocks.FORCED_CHISELABLE), "missing C&B forced tag");
				helper.assertTrue(BlockBitInfo.canChisel(actual), "C&B rejected colored block state");

				var drops = Block.getDrops(actual, helper.getLevel(), absolutePlaced, null);
				helper.assertValueEqual(drops.size(), 1, "drop count");
				assertStack(helper, block, drops.getFirst(), shade, "drop stack");
				assertStack(
						helper,
						block,
						actual.getCloneItemStack(helper.getLevel(), absolutePlaced, false),
						shade,
						"clone stack");
			}
		}

		helper.succeed();
	}

	@GameTest(maxTicks = 200)
	public void crafterRecipeAndExport(GameTestHelper helper) throws Exception {
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		Inventory inventory = player.getInventory();
		inventory.clearContent();
		inventory.setItem(0, new ItemStack(Items.COBBLESTONE, 4));
		inventory.setItem(1, new ItemStack(Items.GLASS, 4));
		inventory.setItem(2, new ItemStack(Items.GLOWSTONE, 4));
		int slot = 3;
		for (DyeColor dye : DyeColor.values()) {
			inventory.setItem(slot++, new ItemStack(dyeItem(dye), 4));
		}

		ContainerColoredBlockCrafter menu =
				new ContainerColoredBlockCrafter(1, inventory, FlatColoredBlocks.CRAFTING_SETTINGS);
		helper.assertValueEqual(
				menu.getItemCount(), FlatColoredBlocks.getFullNumberOfShades(), "all Crafter options");
		helper.assertFalse(menu.clickMenuButton(player, -1), "negative scroll row accepted");
		helper.assertFalse(menu.clickMenuButton(player, menu.maxScrollRows() + 1), "oversized scroll row accepted");
		helper.assertTrue(menu.clickMenuButton(player, menu.maxScrollRows()), "valid scroll row rejected");
		helper.assertValueEqual(menu.getScrollRow(), menu.maxScrollRows(), "Crafter max scroll row");
		menu.setScrollRow(0);

		BlockFlatColored normal = FlatColoredBlocks.first(EnumFlatBlockType.NORMAL);
		BlockState targetState = normal.defaultBlockState().setValue(normal.shadeProperty(), 0);
		Set<DyeColor> requiredDyes = new LinkedHashSet<>();
		for (EnumFlatColorAttributes attribute : normal.getFlatColorAttributes(targetState)) {
			requiredDyes.add(attribute.primaryDye);
			requiredDyes.add(attribute.secondaryDye);
		}
		int cobblestoneBeforeClick = count(inventory, Items.COBBLESTONE);
		int[] dyeCountsBeforeClick = requiredDyes.stream()
				.mapToInt(dye -> count(inventory, dyeItem(dye)))
				.toArray();
		var outputSlot = menu.getSlot(0);
		ItemStack clicked = outputSlot.getItem().copy();
		helper.assertTrue(outputSlot.mayPickup(player), "Crafter normal-click output was unavailable");
		outputSlot.onTake(player, clicked);
		helper.assertValueEqual(
				clicked.getCount(),
				FlatColoredBlocks.CRAFTING_SETTINGS.solidOutput(),
				"normal-click Crafter output count");
		helper.assertValueEqual(
				count(inventory, Items.COBBLESTONE),
				cobblestoneBeforeClick - 1,
				"normal-click base material consumption");
		int clickDyeIndex = 0;
		for (DyeColor dye : requiredDyes) {
			helper.assertValueEqual(
					count(inventory, dyeItem(dye)),
					dyeCountsBeforeClick[clickDyeIndex++] - 1,
					"normal-click dye consumption for " + dye);
		}

		int cobblestoneBefore = count(inventory, Items.COBBLESTONE);
		int[] dyeCounts = requiredDyes.stream().mapToInt(dye -> count(inventory, dyeItem(dye))).toArray();

		ItemStack crafted = menu.quickMoveStack(player, 0);
		int crafts = cobblestoneBefore;
		helper.assertValueEqual(
				crafted.getCount(),
				crafts * FlatColoredBlocks.CRAFTING_SETTINGS.solidOutput(),
				"Crafter output count");
		helper.assertValueEqual(
				normal.stateFromStack(crafted).getValue(normal.shadeProperty()), 0, "Crafter output shade");
		helper.assertValueEqual(
				count(inventory, Items.COBBLESTONE), cobblestoneBefore - crafts, "base material consumption");
		int dyeIndex = 0;
		for (DyeColor dye : requiredDyes) {
			helper.assertValueEqual(
						count(inventory, dyeItem(dye)),
						dyeCounts[dyeIndex++] - crafts,
						"dye consumption for " + dye);
		}

		ResourceKey<Recipe<?>> recipeKey =
				ResourceKey.create(Registries.RECIPE, FlatColoredBlocks.id("crafting_item_recipe"));
		helper.assertTrue(
				helper.getLevel().getServer().getRecipeManager().byKey(recipeKey).isPresent(),
				"Crafter item recipe was not loaded");
		helper.assertTrue(
				helper.getLevel().getServer().getCommands().getDispatcher()
						.findNode(List.of("flatcoloredblock_export_list")) != null,
				"export command was not registered");

		Path directory = Files.createTempDirectory("flatcoloredblocks-export");
		Path output = directory.resolve("flatcoloredblocks.csv");
		try {
			ExportFCBlockList.writeCsv(output);
			List<String> lines = Files.readAllLines(output);
			helper.assertValueEqual(
					lines.size(), FlatColoredBlocks.getFullNumberOfShades() + 1, "CSV row count");
			helper.assertTrue(lines.getFirst().startsWith("\"Registry ID\""), "CSV header");
			helper.assertTrue(lines.getLast().contains("\"1289\""), "CSV final shade");
			helper.assertFalse(Files.exists(output.resolveSibling(output.getFileName() + ".tmp")), "CSV temp file");
		} finally {
			Files.deleteIfExists(output);
			Files.deleteIfExists(directory);
		}

		helper.succeed();
	}

	@GameTest(maxTicks = 400)
	public void upgradesLegacyWorldAndChiseledData(GameTestHelper helper) {
		Path levelPath = Path.of("legacy-flatcoloredblocks-level.dat").toAbsolutePath();
		CompoundTag levelData = legacyLevelData();
		LegacyFCBBlockFix.capture(levelPath, levelData);
		LegacyFCBBlockFix.activate(levelPath);
		LegacyChiseledBlockFix.captureForgeRegistry(levelPath, levelData);
		CompoundTag legacyFml = LegacyChiseledBlockFix.activateForgeRegistry(levelPath);

		CompoundTag savedLevelData = new CompoundTag();
		LegacyChiseledBlockFix.preserveForgeRegistry(savedLevelData, legacyFml);
		LegacyFCBBlockFix.preserve(savedLevelData);
		helper.assertValueEqual(
				savedLevelData.getCompoundOrEmpty("FML"),
				levelData.getCompoundOrEmpty("FML"),
				"legacy Forge registry for lazy chunks");

		BlockFlatColored normal = FlatColoredBlocks.first(EnumFlatBlockType.NORMAL);
		BlockFlatColored transparent = FlatColoredBlocks.first(EnumFlatBlockType.TRANSPARENT);
		BlockFlatColored glowing = FlatColoredBlocks.first(EnumFlatBlockType.GLOWING);
		assertLegacyState(helper, LegacyFCBBlockFix.convertChunkState(300 << 4), normal, 0, "solid chunk state");
		assertLegacyState(
				helper,
				LegacyFCBBlockFix.convertChunkState(301 << 4 | 5),
				transparent,
				645,
				"transparent chunk state");
		assertLegacyState(
				helper,
				LegacyFCBBlockFix.convertChunkState(302 << 4 | 9),
				glowing,
				1289,
				"glowing chunk state");
		helper.assertTrue(
				LegacyFCBBlockFix.convertChunkState(303 << 4 | 10) == null,
				"invalid legacy shade was accepted");
		helper.assertTrue(LegacyFCBBlockFix.convertChunkState(1 << 4) == null, "vanilla state was intercepted");

		VoxelBlob legacyBlob = new VoxelBlob();
		legacyBlob.set(0, 0, 0, 1);
		legacyBlob.set(1, 0, 0, 300);
		legacyBlob.set(2, 0, 0, 5 << 12 | 301);
		legacyBlob.set(3, 0, 0, 9 << 12 | 302);
		CompoundTag legacyBlockEntity = new CompoundTag();
		legacyBlockEntity.putString("id", "minecraft:mod.chiselsandbits.tileentitychiseled");
		legacyBlockEntity.putInt("x", 1);
		legacyBlockEntity.putInt("y", 64);
		legacyBlockEntity.putInt("z", 1);
		legacyBlockEntity.putInt("b", 9 << 12 | 302);
		legacyBlockEntity.putByteArray("v", legacyBlob.toLegacyByteArray());
		legacyBlockEntity.putInt("s", 0);
		legacyBlockEntity.putInt("lv", 0);
		legacyBlockEntity.putBoolean("nc", false);

		Dynamic<?> converted = LegacyChiseledBlockFix.convertBlockEntity(
				new Dynamic<>(NbtOps.INSTANCE, legacyBlockEntity));
		helper.assertTrue(converted != null, "legacy C&B block entity was not converted");
		CompoundTag fixed = (CompoundTag) converted.convert(NbtOps.INSTANCE).getValue();
		helper.assertValueEqual(fixed.getStringOr("id", ""), "chiselsandbits:chiseled", "C&B block entity id");
		helper.assertFalse(fixed.contains("v"), "legacy C&B voxel field");
		helper.assertTrue(fixed.contains("X"), "current C&B voxel field");

		BlockPos position = new BlockPos(1, 64, 1);
		TileEntityBlockChiseled loaded = loadChiseled(position, fixed, helper);
		assertVoxel(helper, loaded, 0, Blocks.STONE.defaultBlockState(), "stone voxel");
		assertVoxel(helper, loaded, 1, state(normal, 0), "solid FCB voxel");
		assertVoxel(helper, loaded, 2, state(transparent, 645), "transparent FCB voxel");
		assertVoxel(helper, loaded, 3, state(glowing, 1289), "glowing FCB voxel");
		helper.assertValueEqual(loaded.getBlob().filled(), 4, "C&B filled voxel count");
		helper.assertValueEqual(
				loaded.getPrimaryBlockStateId(), ModUtil.getStateId(state(glowing, 1289)), "C&B primary state");

		CompoundTag saved = loaded.saveWithFullMetadata(helper.getLevel().registryAccess());
		TileEntityBlockChiseled reloaded = loadChiseled(position, saved, helper);
		helper.assertValueEqual(reloaded.getBlob(), loaded.getBlob(), "C&B save/reload voxel data");
		helper.assertValueEqual(
				reloaded.getPrimaryBlockStateId(), loaded.getPrimaryBlockStateId(), "C&B save/reload primary state");

		CompoundTag fixedChunk = DataFixTypes.CHUNK.updateToCurrentVersion(
				DataFixers.getDataFixer(), legacyChunk(legacyBlockEntity), DATA_VERSION_1_12_2);
		String chunkText = fixedChunk.toString();
		helper.assertTrue(chunkText.contains("flatcoloredblocks:flatcoloredblock"), "solid chunk block migration");
		helper.assertTrue(
				chunkText.contains("flatcoloredblocks:flatcoloredblock_transparent_127"),
				"transparent chunk block migration");
		helper.assertTrue(
				chunkText.contains("flatcoloredblocks:flatcoloredblock_glowing_255"),
				"glowing chunk block migration");
		helper.assertTrue(chunkText.contains("shade:\"645\""), "transparent chunk shade migration");
		helper.assertTrue(chunkText.contains("shade:\"1289\""), "glowing chunk shade migration");
		helper.assertTrue(chunkText.contains("chiselsandbits:chiseled_block"), "C&B container block migration");

		LegacyChiseledBlockFix.captureForgeRegistry(new CompoundTag());
		LegacyFCBBlockFix.capture(levelPath, new CompoundTag());
		LegacyFCBBlockFix.activate(levelPath);
		helper.succeed();
	}

	@GameTest(maxTicks = 200)
	public void beaconUsesAllFlatColoredTypes(GameTestHelper helper) {
		BlockPos beaconPosition = new BlockPos(3, 2, 3);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				helper.setBlock(beaconPosition.offset(x, -1, z), Blocks.IRON_BLOCK);
			}
		}
		helper.setBlock(beaconPosition, Blocks.BEACON);

		BlockFlatColored normal = FlatColoredBlocks.first(EnumFlatBlockType.NORMAL);
		BlockFlatColored transparent = FlatColoredBlocks.first(EnumFlatBlockType.TRANSPARENT);
		BlockFlatColored glowing = FlatColoredBlocks.first(EnumFlatBlockType.GLOWING);
		helper.setBlock(beaconPosition.above(1), state(normal, 0));
		helper.setBlock(beaconPosition.above(2), state(transparent, 645));
		helper.setBlock(beaconPosition.above(3), state(glowing, 1289));

		BlockPos absolute = helper.absolutePos(beaconPosition);
		BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolute);
		helper.assertTrue(blockEntity instanceof BeaconBlockEntity, "beacon block entity");
		BeaconBlockEntity beacon = (BeaconBlockEntity) blockEntity;
		int normalColor = 0xff000000 | normal.colorFromShade(0);
		int transparentColor = 0xff000000 | transparent.colorFromShade(645);
		int glowingColor = 0xff000000 | glowing.colorFromShade(1289);
		int firstAverage = ARGB.average(normalColor, transparentColor);
		helper.succeedWhen(() -> {
			var sections = beacon.getBeamSections();
			helper.assertValueEqual(sections.size(), 4, "beacon beam section count");
			helper.assertValueEqual(sections.get(1).getColor(), normalColor, "normal beam color");
			helper.assertValueEqual(
					sections.get(2).getColor(), firstAverage, "transparent beam average");
			helper.assertValueEqual(
					sections.get(3).getColor(),
					ARGB.average(firstAverage, glowingColor),
					"glowing beam average");
		});
	}

	private static void assertStack(
			GameTestHelper helper, BlockFlatColored block, ItemStack stack, int shade, String source) {
		helper.assertTrue(stack.getItem() == block.asItem(), source + " item");
		BlockItemStateProperties states =
				stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		helper.assertValueEqual(states.get(block.shadeProperty()), shade, source + " component shade");
		helper.assertValueEqual(
				block.stateFromStack(stack).getValue(block.shadeProperty()), shade, source + " restored shade");
		CustomModelData modelData = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);
		helper.assertValueEqual(modelData.getColor(0), block.colorForTint(shade), source + " item tint");
	}

	private static Item dyeItem(DyeColor dye) {
		return BuiltInRegistries.ITEM.getValue(
				net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", dye.getName() + "_dye"));
	}

	private static int count(Inventory inventory, Item item) {
		int total = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static BlockState state(BlockFlatColored block, int shade) {
		return block.defaultBlockState().setValue(block.shadeProperty(), shade);
	}

	private static void assertLegacyState(
			GameTestHelper helper,
			Dynamic<?> dynamic,
			BlockFlatColored expectedBlock,
			int expectedShade,
			String source) {
		helper.assertTrue(dynamic != null, source + " was not converted");
		CompoundTag tag = (CompoundTag) dynamic.convert(NbtOps.INSTANCE).getValue();
		BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag);
		helper.assertTrue(state.is(expectedBlock), source + " block");
		helper.assertValueEqual(state.getValue(expectedBlock.shadeProperty()), expectedShade, source + " shade");
	}

	private static TileEntityBlockChiseled loadChiseled(
			BlockPos position, CompoundTag tag, GameTestHelper helper) {
		BlockEntity loaded = BlockEntity.loadStatic(
				position,
				ModBlocks.getChiseledDefaultState(),
				tag,
				helper.getLevel().registryAccess());
		helper.assertTrue(loaded instanceof TileEntityBlockChiseled, "converted C&B block entity did not load");
		TileEntityBlockChiseled chiseled = (TileEntityBlockChiseled) loaded;
		chiseled.setLevel(helper.getLevel());
		return chiseled;
	}

	private static void assertVoxel(
			GameTestHelper helper,
			TileEntityBlockChiseled blockEntity,
			int x,
			BlockState expected,
			String source) {
		helper.assertValueEqual(
				blockEntity.getBlob().get(x, 0, 0), ModUtil.getStateId(expected), source);
	}

	private static CompoundTag legacyLevelData() {
		ListTag ids = new ListTag();
		ids.add(registryEntry("chiselsandbits:chiseled_rock", 256));
		ids.add(registryEntry("flatcoloredblocks:flatcoloredblock0", 300));
		ids.add(registryEntry("flatcoloredblocks:flatcoloredblock_transparent0_40", 301));
		ids.add(registryEntry("flatcoloredblocks:flatcoloredblock_glowing0_80", 302));
		ids.add(registryEntry("flatcoloredblocks:flatcoloredblock_glowing0_80", 303));

		CompoundTag blockRegistry = new CompoundTag();
		blockRegistry.put("ids", ids);
		CompoundTag registries = new CompoundTag();
		registries.put("minecraft:blocks", blockRegistry);
		CompoundTag fml = new CompoundTag();
		fml.put("Registries", registries);
		CompoundTag root = new CompoundTag();
		root.put("FML", fml);
		return root;
	}

	private static CompoundTag registryEntry(String name, int id) {
		CompoundTag entry = new CompoundTag();
		entry.putString("K", name);
		entry.putInt("V", id);
		return entry;
	}

	private static CompoundTag legacyChunk(CompoundTag blockEntity) {
		byte[] blocks = new byte[4096];
		byte[] data = new byte[2048];
		byte[] add = new byte[2048];
		setLegacyBlock(blocks, data, add, index(2, 1, 2), 300, 0);
		setLegacyBlock(blocks, data, add, index(3, 1, 2), 301, 5);
		setLegacyBlock(blocks, data, add, index(4, 1, 2), 302, 9);

		CompoundTag section = new CompoundTag();
		section.putByte("Y", (byte) 4);
		section.putByteArray("Blocks", blocks);
		section.putByteArray("Data", data);
		section.putByteArray("Add", add);
		section.putByteArray("BlockLight", new byte[2048]);
		section.putByteArray("SkyLight", new byte[2048]);
		ListTag sections = new ListTag();
		sections.add(section);

		ListTag blockEntities = new ListTag();
		blockEntities.add(blockEntity.copy());
		CompoundTag level = new CompoundTag();
		level.putInt("xPos", 0);
		level.putInt("zPos", 0);
		level.putLong("LastUpdate", 0);
		level.putLong("InhabitedTime", 0);
		level.putBoolean("TerrainPopulated", true);
		level.putBoolean("LightPopulated", true);
		level.put("Sections", sections);
		level.put("TileEntities", blockEntities);
		level.put("Entities", new ListTag());
		level.put("TileTicks", new ListTag());
		level.putByteArray("Biomes", new byte[256]);
		level.putIntArray("HeightMap", new int[256]);

		CompoundTag chunk = new CompoundTag();
		chunk.putInt("DataVersion", DATA_VERSION_1_12_2);
		chunk.put("Level", level);
		return chunk;
	}

	private static int index(int x, int y, int z) {
		return x | z << 4 | y << 8;
	}

	private static void setLegacyBlock(
			byte[] blocks, byte[] data, byte[] add, int index, int blockId, int metadata) {
		blocks[index] = (byte) blockId;
		setNibble(data, index, metadata);
		setNibble(add, index, blockId >>> 8);
	}

	private static void setNibble(byte[] values, int index, int value) {
		int byteIndex = index >>> 1;
		int shift = (index & 1) * 4;
		values[byteIndex] = (byte) ((values[byteIndex] & 0xff & ~(15 << shift)) | (value & 15) << shift);
	}
}
