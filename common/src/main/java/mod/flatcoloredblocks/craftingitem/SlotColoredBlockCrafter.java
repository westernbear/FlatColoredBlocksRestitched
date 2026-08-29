package mod.flatcoloredblocks.craftingitem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class SlotColoredBlockCrafter extends Slot {
	private final InventoryColoredBlockCrafter craftingInventory;

	SlotColoredBlockCrafter(
			InventoryColoredBlockCrafter inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.craftingInventory = inventory;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public boolean mayPickup(Player player) {
		return !craftingInventory.craft(getItem(), 1, true).isEmpty();
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		craftingInventory.craft(stack, 1, false);
		super.onTake(player, stack);
	}
}
