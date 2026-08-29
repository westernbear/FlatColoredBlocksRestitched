package mod.flatcoloredblocks.datafixer;

import com.mojang.serialization.Dynamic;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mod.chiselsandbits.helpers.ModUtil;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.compat.ChiselsAndBitsCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LegacyFCBBlockFix {
	private static final Logger LOGGER = LoggerFactory.getLogger("FlatColoredBlocks/LegacyFix");
	private static final String PREFIX = "flatcoloredblocks:flatcoloredblock";
	private static final int LEGACY_MAX_SHADE = 1289;
	private static final Snapshot EMPTY = new Snapshot(Map.of(), null);
	private static final ThreadLocal<PendingSnapshot> PENDING = new ThreadLocal<>();
	private static final Set<String> WARNED_CLAMPS = ConcurrentHashMap.newKeySet();
	private static volatile Snapshot active = EMPTY;

	private LegacyFCBBlockFix() {}

	public static void capture(Path path, CompoundTag root) {
		PENDING.set(new PendingSnapshot(path.toAbsolutePath().normalize(), read(root)));
	}

	public static void activate(Path path) {
		PendingSnapshot pending = PENDING.get();
		PENDING.remove();
		active = pending != null && pending.path().equals(path.toAbsolutePath().normalize())
				? pending.snapshot()
				: EMPTY;
	}

	public static void preserve(CompoundTag root) {
		if (active.fml() != null) {
			root.put("FML", active.fml().copy());
		}
	}

	public static Dynamic<?> convertChunkState(int legacyState) {
		BlockState state = resolve(legacyState >>> 4, legacyState & 15);
		return state == null ? null : new Dynamic<>(NbtOps.INSTANCE, NbtUtils.writeBlockState(state));
	}

	public static Integer convertChiselsAndBitsState(int legacyState) {
		if (!ChiselsAndBitsCompat.isLoaded()) {
			return null;
		}
		BlockState state = resolve(legacyState & 4095, legacyState >>> 12 & 15);
		return state == null ? null : ModUtil.getStateId(state);
	}

	private static Snapshot read(CompoundTag root) {
		if (root == null) {
			return EMPTY;
		}

		CompoundTag fml = root.getCompound("FML").orElse(null);
		if (fml == null) {
			fml = root.getCompoundOrEmpty("Data").getCompound("FML").orElse(null);
		}
		if (fml == null) {
			return EMPTY;
		}

		ListTag ids = fml.getCompoundOrEmpty("Registries")
				.getCompoundOrEmpty("minecraft:blocks")
				.getListOrEmpty("ids");
		Map<Integer, String> blocks = new HashMap<>();
		for (Tag value : ids) {
			if (!(value instanceof CompoundTag entry)) {
				continue;
			}
			int id = entry.getIntOr("V", -1);
			String name = entry.getStringOr("K", "").toLowerCase(Locale.ROOT);
			if (id >= 0 && id <= 4095 && name.startsWith(PREFIX)) {
				blocks.put(id, name);
			}
		}
		return blocks.isEmpty() ? EMPTY : new Snapshot(Map.copyOf(blocks), fml.copy());
	}

	private static BlockState resolve(int blockId, int metadata) {
		LegacyBlock legacy = parse(active.blocks().get(blockId));
		if (legacy == null) {
			return null;
		}

		int shade = legacy.group() * 16 + metadata;
		if (shade < 0 || shade > LEGACY_MAX_SHADE) {
			return null;
		}

		int wantedVariant = switch (legacy.type()) {
			case NORMAL -> 255;
			case TRANSPARENT -> 127;
			case GLOWING -> 255;
		};
		BlockFlatColored block = FlatColoredBlocks.BLOCKS.stream()
				.filter(candidate -> candidate.getType() == legacy.type())
				.min(Comparator.comparingInt(candidate -> Math.abs(candidate.getVariant() - wantedVariant)))
				.orElse(null);
		if (block == null) {
			return null;
		}

		int currentShade = Math.min(shade, block.getMaxShade());
		if (currentShade != shade && WARNED_CLAMPS.add(block.registryName())) {
			LOGGER.warn(
					"Legacy shade values exceed the configured range for {}; values will be clamped to {}",
					block.registryName(),
					block.getMaxShade());
		}
		return block.defaultBlockState().setValue(block.shadeProperty(), currentShade);
	}

	private static LegacyBlock parse(String name) {
		if (name == null) {
			return null;
		}
		if (name.startsWith(PREFIX + "_transparent0_")) {
			return parseGroup(name, PREFIX + "_transparent0_", EnumFlatBlockType.TRANSPARENT);
		}
		if (name.startsWith(PREFIX + "_glowing0_")) {
			return parseGroup(name, PREFIX + "_glowing0_", EnumFlatBlockType.GLOWING);
		}
		return parseGroup(name, PREFIX, EnumFlatBlockType.NORMAL);
	}

	private static LegacyBlock parseGroup(String name, String prefix, EnumFlatBlockType type) {
		try {
			return new LegacyBlock(type, Integer.parseInt(name.substring(prefix.length())));
		} catch (NumberFormatException error) {
			return null;
		}
	}

	private record LegacyBlock(EnumFlatBlockType type, int group) {}

	private record Snapshot(Map<Integer, String> blocks, CompoundTag fml) {}

	private record PendingSnapshot(Path path, Snapshot snapshot) {}
}
