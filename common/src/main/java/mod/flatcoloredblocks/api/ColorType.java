package mod.flatcoloredblocks.api;

import java.util.Objects;
import java.util.Optional;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import net.minecraft.resources.Identifier;

public final class ColorType {
	private final Identifier id;
	private final EnumFlatBlockType builtin;

	private ColorType(Identifier id, EnumFlatBlockType builtin) {
		this.id = Objects.requireNonNull(id);
		this.builtin = builtin;
	}

	public static ColorType ofBuiltin(EnumFlatBlockType type) {
		return new ColorType(FlatColoredBlocks.id(type.name().toLowerCase()), type);
	}

	public static ColorType ofCustom(Identifier id) {
		return new ColorType(id, null);
	}

	public static Optional<ColorType> builtin(Identifier id) {
		for (EnumFlatBlockType type : EnumFlatBlockType.values()) {
			ColorType colorType = ofBuiltin(type);
			if (colorType.id.equals(id)) {
				return Optional.of(colorType);
			}
		}
		return Optional.empty();
	}

	public Identifier id() {
		return id;
	}

	public boolean isBuiltin() {
		return builtin != null;
	}

	public Optional<EnumFlatBlockType> builtinType() {
		return Optional.ofNullable(builtin);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ColorType other && id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "ColorType[" + id + "]";
	}
}
