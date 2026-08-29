package mod.flatcoloredblocks.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mod.flatcoloredblocks.block.BlockFlatColored;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconBlockEntityMixin {
	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColor()I"),
			require = 1)
	private static int fcbr$useFlatColoredShade(
			int original,
			@Local(name = "state") BlockState state) {
		if (state.getBlock() instanceof BlockFlatColored block) {
			return 0xff000000 | block.colorFromShade(block.getShadeNumber(state));
		}
		return original;
	}
}
