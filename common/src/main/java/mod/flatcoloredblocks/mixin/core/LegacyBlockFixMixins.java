package mod.flatcoloredblocks.mixin.core;

import com.mojang.serialization.Dynamic;
import mod.chiselsandbits.legacy.LegacyChiseledBlockFix;
import mod.flatcoloredblocks.datafixer.LegacyFCBBlockFix;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class LegacyBlockFixMixins {
	private LegacyBlockFixMixins() {}

	@Mixin(LevelStorageSource.class)
	public static class LevelDataMixin {
		@Inject(
				method = "readLevelDataTagRaw(Ljava/nio/file/Path;)Lnet/minecraft/nbt/CompoundTag;",
				at = @At("RETURN"),
				require = 1)
		private static void flatcoloredblocks$captureRegistry(
				java.nio.file.Path path, CallbackInfoReturnable<CompoundTag> callback) {
			LegacyFCBBlockFix.capture(path, callback.getReturnValue());
		}
	}

	@Mixin(LevelStorageSource.LevelStorageAccess.class)
	public static class LevelDataSaveMixin {
		@Shadow
		@Final
		private LevelStorageSource.LevelDirectory levelDirectory;

		@Inject(
				method = "getUnfixedDataTag(Z)Lcom/mojang/serialization/Dynamic;",
				at = @At("RETURN"),
				require = 1)
		private void flatcoloredblocks$activateRegistry(
				boolean oldData, CallbackInfoReturnable<Dynamic<?>> callback) {
			LegacyFCBBlockFix.activate(oldData ? levelDirectory.oldDataFile() : levelDirectory.dataFile());
		}

		@Inject(
				method = "saveLevelData(Lnet/minecraft/nbt/CompoundTag;)V",
				at = @At("HEAD"),
				require = 1)
		private void flatcoloredblocks$preserveRegistry(CompoundTag root, CallbackInfo callback) {
			LegacyFCBBlockFix.preserve(root);
		}
	}

	@Mixin(targets = "net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix$Section")
	public static class SectionMixin {
		@Redirect(
				method = "upgrade",
				at = @At(
						value = "INVOKE",
						target = "Lnet/minecraft/util/datafix/fixes/BlockStateData;getTag(I)Lcom/mojang/serialization/Dynamic;"),
				require = 1)
		private Dynamic<?> flatcoloredblocks$convertLegacyState(int legacyState) {
			Dynamic<?> converted = LegacyFCBBlockFix.convertChunkState(legacyState);
			return converted == null ? BlockStateData.getTag(legacyState) : converted;
		}
	}

	@Mixin(value = LegacyChiseledBlockFix.class, remap = false)
	public static class ChiselsAndBitsMixin {
		@Inject(method = "resolveLegacyState", at = @At("HEAD"), cancellable = true, require = 1)
		private static void flatcoloredblocks$convertLegacyState(
				int legacyState, CallbackInfoReturnable<Integer> callback) {
			Integer converted = LegacyFCBBlockFix.convertChiselsAndBitsState(legacyState);
			if (converted != null) {
				callback.setReturnValue(converted);
			}
		}
	}
}
