package mod.flatcoloredblocks.craftingitem;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

final class SlotChangeDetect extends Slot {
	private final InventoryColoredBlockCrafter craftingInventory;

	SlotChangeDetect(
			Container inventory,
			InventoryColoredBlockCrafter craftingInventory,
			int index,
			int x,
			int y) {
		super(inventory, index, x, y);
		this.craftingInventory = craftingInventory;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		craftingInventory.refreshOptions();
	}
}
