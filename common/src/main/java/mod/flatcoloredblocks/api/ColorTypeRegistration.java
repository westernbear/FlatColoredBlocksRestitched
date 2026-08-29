package mod.flatcoloredblocks.api;

import java.util.Objects;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import net.minecraft.resources.Identifier;

/**
 * Describes an addon-registered color type.
 *
 * @param id unique type id (recommended {@code yourmod:name})
 * @param blockNamePrefix registry path prefix for generated blocks
 * @param template which built-in HSV/config template to copy (normal / transparent / glowing)
 */
public record ColorTypeRegistration(
		Identifier id,
		String blockNamePrefix,
		EnumFlatBlockType template) {

	public ColorTypeRegistration {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(blockNamePrefix, "blockNamePrefix");
		Objects.requireNonNull(template, "template");
		if (blockNamePrefix.isBlank()) {
			throw new IllegalArgumentException("blockNamePrefix must not be blank");
		}
	}

	public static ColorTypeRegistration of(Identifier id, String blockNamePrefix, EnumFlatBlockType template) {
		return new ColorTypeRegistration(id, blockNamePrefix, template);
	}
}
