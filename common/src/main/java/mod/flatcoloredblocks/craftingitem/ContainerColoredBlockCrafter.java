package mod.flatcoloredblocks.craftingitem;

import mod.flatcoloredblocks.FlatColoredBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ContainerColoredBlockCrafter extends AbstractContainerMenu {
	public static final int OUTPUT_COLUMNS = 9;
	public static final int OUTPUT_ROWS = 7;
	public static final int OUTPUT_SLOTS = OUTPUT_COLUMNS * OUTPUT_ROWS;
	public static final int PLAYER_START = OUTPUT_SLOTS;
	public static final int PLAYER_END = PLAYER_START + 36;

	private final Player player;
	private final Inventory playerInventory;
	private final InventoryColoredBlockCrafter craftingInventory;
	private int scrollRow;

	public ContainerColoredBlockCrafter(
			int containerId, Inventory playerInventory, CraftingSettings settings) {
		super(FlatColoredBlocks.CRAFTER_MENU, containerId);
		this.player = playerInventory.player;
		this.playerInventory = playerInventory;
		this.craftingInventory = new InventoryColoredBlockCrafter(player, this, settings);
		this.craftingInventory.refreshOptions();

		for (int row = 0; row < OUTPUT_ROWS; row++) {
			for (int column = 0; column < OUTPUT_COLUMNS; column++) {
				addSlot(new SlotColoredBlockCrafter(
						craftingInventory,
						row * OUTPUT_COLUMNS + column,
						8 + column * 18,
						18 + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new SlotChangeDetect(
						playerInventory,
						craftingInventory,
						column + row * 9 + 9,
						8 + column * 18,
						158 + row * 18));
			}
		}
		for (int column = 0; column < 9; column++) {
			addSlot(new SlotChangeDetect(
					playerInventory,
					craftingInventory,
					column,
					8 + column * 18,
					216));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId) {
		if (buttonId < 0 || buttonId > maxScrollRows()) {
			return false;
		}
		setScrollRow(buttonId);
		return true;
	}

	public void setScrollRow(int row) {
		scrollRow = Math.clamp(row, 0, maxScrollRows());
		craftingInventory.setOffset(scrollRow * OUTPUT_COLUMNS);
	}

	void clampScroll() {
		setScrollRow(scrollRow);
	}

	public int getScrollRow() {
		return scrollRow;
	}

	public int maxScrollRows() {
		return Math.max((craftingInventory.optionCount() + OUTPUT_COLUMNS - 1) / OUTPUT_COLUMNS - OUTPUT_ROWS, 0);
	}

	public int getItemCount() {
		return craftingInventory.optionCount();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index < 0 || index >= OUTPUT_SLOTS) {
			return ItemStack.EMPTY;
		}
		Slot slot = slots.get(index);
		ItemStack target = slot.getItem();
		if (target.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack oneCraft = craftingInventory.craft(target, 1, true);
		if (oneCraft.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int capacity = playerCapacity(oneCraft);
		int crafts = Math.min(64 / oneCraft.getCount(), capacity / oneCraft.getCount());
		ItemStack crafted = craftingInventory.craft(target, crafts, false);
		if (crafted.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack result = crafted.copy();
		moveItemStackTo(crafted, PLAYER_START, PLAYER_END, true);
		if (!crafted.isEmpty()) {
			player.drop(crafted, false);
		}
		return result;
	}

	private int playerCapacity(ItemStack target) {
		int capacity = 0;
		for (int slot = 0; slot < Math.min(36, playerInventory.getContainerSize()); slot++) {
			ItemStack present = playerInventory.getItem(slot);
			if (present.isEmpty()) {
				capacity += target.getMaxStackSize();
			} else if (ItemStack.isSameItemSameComponents(present, target)) {
				capacity += Math.max(0, present.getMaxStackSize() - present.getCount());
			}
		}
		return capacity;
	}

	public InventoryColoredBlockCrafter craftingInventory() {
		return craftingInventory;
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		craftingInventory.refreshOptions();
	}
}
