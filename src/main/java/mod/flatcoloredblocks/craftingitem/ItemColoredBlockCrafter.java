package mod.flatcoloredblocks.craftingitem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mod.flatcoloredblocks.FlatColoredBlocks;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class ItemColoredBlockCrafter extends Item {
	public ItemColoredBlockCrafter(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new ExtendedMenuProvider<CraftingSettings>() {
				@Override
				public CraftingSettings getScreenOpeningData(ServerPlayer player) {
					return FlatColoredBlocks.CRAFTING_SETTINGS;
				}

				@Override
				public Component getDisplayName() {
					return Component.translatable("item.flatcoloredblocks.coloredcraftingitem");
				}

				@Override
				public AbstractContainerMenu createMenu(
						int containerId, Inventory inventory, Player player) {
					return new ContainerColoredBlockCrafter(
							containerId,
							inventory,
							FlatColoredBlocks.CRAFTING_SETTINGS);
				}
			});
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> output,
			TooltipFlag flag) {
		List<Item> materials = new ArrayList<>();
		for (var type : mod.flatcoloredblocks.block.EnumFlatBlockType.values()) {
			for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(
					FlatColoredBlocks.CRAFTING_SETTINGS.tag(type))) {
				materials.add(holder.value());
			}
		}

		if (!materials.isEmpty()) {
			Item material = materials.get((int) ((Util.getMillis() / 1200) % materials.size()));
			output.accept(Component.translatable(
					"item.flatcoloredblocks.coloredcraftingitem.tip1",
					material.getName(material.getDefaultInstance())));
		}
		output.accept(Component.translatable("item.flatcoloredblocks.coloredcraftingitem.tip2"));
		super.appendHoverText(stack, context, display, output, flag);
	}
}
