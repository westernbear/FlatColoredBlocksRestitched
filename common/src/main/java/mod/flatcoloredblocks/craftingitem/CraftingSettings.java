package mod.flatcoloredblocks.craftingitem;

import io.netty.buffer.ByteBuf;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.config.ModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record CraftingSettings(
		Identifier solidTag,
		Identifier transparentTag,
		Identifier glowingTag,
		int solidOutput,
		int transparentOutput,
		int glowingOutput,
		boolean allowCraftingTable) {

	public static final StreamCodec<ByteBuf, CraftingSettings> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC,
			CraftingSettings::solidTag,
			Identifier.STREAM_CODEC,
			CraftingSettings::transparentTag,
			Identifier.STREAM_CODEC,
			CraftingSettings::glowingTag,
			ByteBufCodecs.VAR_INT,
			CraftingSettings::solidOutput,
			ByteBufCodecs.VAR_INT,
			CraftingSettings::transparentOutput,
			ByteBufCodecs.VAR_INT,
			CraftingSettings::glowingOutput,
			ByteBufCodecs.BOOL,
			CraftingSettings::allowCraftingTable,
			CraftingSettings::new);

	public static CraftingSettings from(ModConfig config) {
		return new CraftingSettings(
				Identifier.parse(config.solidCraftingBlock),
				Identifier.parse(config.transparentCraftingBlock),
				Identifier.parse(config.glowingCraftingBlock),
				config.solidCraftingOutput,
				config.transparentCraftingOutput,
				config.glowingCraftingOutput,
				config.allowCraftingTable);
	}

	public TagKey<Item> tag(EnumFlatBlockType type) {
		return TagKey.create(
				Registries.ITEM,
				switch (type) {
					case NORMAL -> solidTag;
					case TRANSPARENT -> transparentTag;
					case GLOWING -> glowingTag;
				});
	}

	public int output(EnumFlatBlockType type) {
		return switch (type) {
			case NORMAL -> solidOutput;
			case TRANSPARENT -> transparentOutput;
			case GLOWING -> glowingOutput;
		};
	}
}
