package mod.flatcoloredblocks.craftingitem;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.block.EnumFlatColorAttributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class InventoryColoredBlockCrafter implements Container {
	private final Player player;
	private final ContainerColoredBlockCrafter menu;
	private final CraftingSettings settings;
	private final List<ItemStack> options = new ArrayList<>();
	private int offset;

	InventoryColoredBlockCrafter(
			Player player, ContainerColoredBlockCrafter menu, CraftingSettings settings) {
		this.player = player;
		this.menu = menu;
		this.settings = settings;
	}

	void refreshOptions() {
		EnumSet<DyeColor> dyes = EnumSet.noneOf(DyeColor.class);
		EnumSet<EnumFlatBlockType> bases = EnumSet.noneOf(EnumFlatBlockType.class);
		Inventory inventory = player.getInventory();

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			for (DyeColor dye : DyeColor.values()) {
				if (stack.typeHolder().is(dyeTag(dye))) {
					dyes.add(dye);
				}
			}
			for (EnumFlatBlockType type : EnumFlatBlockType.values()) {
				if (stack.typeHolder().is(settings.tag(type))) {
					bases.add(type);
				}
			}
		}

		options.clear();
		for (BlockFlatColored block : FlatColoredBlocks.BLOCKS) {
			if (!bases.contains(block.getCraftable())) {
				continue;
			}
			int output = settings.output(block.getCraftable());
			for (int shade = 0; shade < block.getNumberOfShades(); shade++) {
				BlockState state = block.defaultBlockState().setValue(block.shadeProperty(), shade);
				if (hasDyes(block.getFlatColorAttributes(state), dyes)) {
					options.add(block.stackForShade(shade, output));
				}
			}
		}
		menu.clampScroll();
	}

	private static boolean hasDyes(
			Set<EnumFlatColorAttributes> attributes, EnumSet<DyeColor> available) {
		DyeColor alternate = EnumFlatColorAttributes.getAlternateDye(attributes);
		if (alternate != null && available.contains(alternate)) {
			return true;
		}
		for (EnumFlatColorAttributes attribute : attributes) {
			if (!available.contains(attribute.primaryDye)
					|| !available.contains(attribute.secondaryDye)) {
				return false;
			}
		}
		return true;
	}

	ItemStack craft(ItemStack target, int attempts, boolean simulate) {
		if (target.isEmpty() || !(Block.byItem(target.getItem()) instanceof BlockFlatColored block)) {
			return ItemStack.EMPTY;
		}

		int outputPerCraft = settings.output(block.getCraftable());
		int maxAttempts = Math.min(Math.max(attempts, 0), 64 / outputPerCraft);
		Inventory inventory = player.getInventory();
		int[] reserved = new int[inventory.getContainerSize()];
		Set<EnumFlatColorAttributes> attributes =
				block.getFlatColorAttributes(block.stateFromStack(target));
		int crafts = 0;

		for (; crafts < maxAttempts; crafts++) {
			int[] trial = reserved.clone();
			if (!reserve(inventory, settings.tag(block.getCraftable()), trial)) {
				break;
			}

			DyeColor alternate = EnumFlatColorAttributes.getAlternateDye(attributes);
			boolean dyesReserved = alternate != null && reserve(inventory, dyeTag(alternate), trial);
			if (!dyesReserved) {
				dyesReserved = true;
				Set<DyeColor> required = new LinkedHashSet<>();
				for (EnumFlatColorAttributes attribute : attributes) {
					required.add(attribute.primaryDye);
					required.add(attribute.secondaryDye);
				}
				for (DyeColor dye : required) {
					if (!reserve(inventory, dyeTag(dye), trial)) {
						dyesReserved = false;
						break;
					}
				}
			}
			if (!dyesReserved) {
				break;
			}
			reserved = trial;
		}

		if (crafts == 0) {
			return ItemStack.EMPTY;
		}
		if (!simulate) {
			for (int slot = 0; slot < reserved.length; slot++) {
				if (reserved[slot] > 0) {
					inventory.removeItem(slot, reserved[slot]);
				}
			}
			inventory.setChanged();
			refreshOptions();
		}
		return target.copyWithCount(crafts * outputPerCraft);
	}

	private static boolean reserve(Inventory inventory, TagKey<Item> tag, int[] reserved) {
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty()
					&& stack.typeHolder().is(tag)
					&& stack.getCount() > reserved[slot]) {
				reserved[slot]++;
				return true;
			}
		}
		return false;
	}

	static TagKey<Item> dyeTag(DyeColor dye) {
		return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + dye.getSerializedName()));
	}

	void setOffset(int offset) {
		this.offset = Math.max(0, offset);
	}

	int optionCount() {
		return options.size();
	}

	@Override
	public int getContainerSize() {
		return options.size();
	}

	@Override
	public boolean isEmpty() {
		return options.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		int option = index + offset;
		return option >= 0 && option < options.size()
				? options.get(option).copy()
				: ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return getItem(index);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return false;
	}

	@Override
	public void clearContent() {
		options.clear();
	}
}
