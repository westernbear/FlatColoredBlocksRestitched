package mod.flatcoloredblocks.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface CrafterOpenedCallback {
	void onCrafterOpened(Player player);
}
