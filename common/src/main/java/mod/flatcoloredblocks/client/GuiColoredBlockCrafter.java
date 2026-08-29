package mod.flatcoloredblocks.client;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class GuiColoredBlockCrafter
		extends AbstractContainerScreen<ContainerColoredBlockCrafter> {
	private static final Identifier TEXTURE =
			FlatColoredBlocks.id("textures/gui/container/coloredcrafting.png");
	private static final int SCROLL_LEFT = 175;
	private static final int SCROLL_TOP = 18;
	private static final int SCROLL_HEIGHT = 126;
	private static final int KNOB_WIDTH = 12;
	private static final int KNOB_HEIGHT = 15;
	private static final int KNOB_TRAVEL = SCROLL_HEIGHT - KNOB_HEIGHT;
	private boolean scrolling;

	public GuiColoredBlockCrafter(
			ContainerColoredBlockCrafter menu,
			Inventory inventory,
			Component title) {
		super(menu, inventory, title, 195, 239);
		inventoryLabelY = 146;
	}

	@Override
	public void extractBackground(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				TEXTURE,
				leftPos,
				topPos,
				0,
				0,
				imageWidth,
				imageHeight,
				256,
				256);

		int maxRows = menu.maxScrollRows();
		int knobY = topPos + SCROLL_TOP;
		if (maxRows > 0) {
			knobY += Math.round(KNOB_TRAVEL * menu.getScrollRow() / (float) maxRows);
		}
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				TEXTURE,
				leftPos + SCROLL_LEFT,
				knobY,
				maxRows > 0 ? 232 : 244,
				0,
				KNOB_WIDTH,
				KNOB_HEIGHT,
				256,
				256);
	}

	@Override
	public boolean mouseScrolled(
			double mouseX,
			double mouseY,
			double horizontalAmount,
			double verticalAmount) {
		if (menu.maxScrollRows() > 0 && verticalAmount != 0) {
			setScrollRow(menu.getScrollRow() - (int) Math.signum(verticalAmount));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && insideScrollbar(event.x(), event.y())) {
			scrolling = true;
			scrollTo(event.y());
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (scrolling) {
			scrollTo(event.y());
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0) {
			scrolling = false;
		}
		return super.mouseReleased(event);
	}

	private boolean insideScrollbar(double x, double y) {
		int left = leftPos + SCROLL_LEFT;
		int top = topPos + SCROLL_TOP;
		return x >= left && x < left + 14 && y >= top && y < top + SCROLL_HEIGHT;
	}

	private void scrollTo(double mouseY) {
		int maxRows = menu.maxScrollRows();
		if (maxRows == 0) {
			setScrollRow(0);
			return;
		}
		double progress = (mouseY - topPos - SCROLL_TOP - KNOB_HEIGHT / 2.0) / KNOB_TRAVEL;
		setScrollRow(Math.round((float) (Mth.clamp(progress, 0, 1) * maxRows)));
	}

	private void setScrollRow(int row) {
		row = Mth.clamp(row, 0, menu.maxScrollRows());
		if (row == menu.getScrollRow()) {
			return;
		}
		menu.setScrollRow(row);
		if (minecraft.gameMode != null) {
			minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
		}
	}
}
