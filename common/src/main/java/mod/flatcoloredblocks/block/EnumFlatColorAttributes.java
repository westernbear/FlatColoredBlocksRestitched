package mod.flatcoloredblocks.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;

import java.util.Set;

public enum EnumFlatColorAttributes
{
	// non-colors
	black( true, true, DyeColor.BLACK, MapColor.COLOR_BLACK ),
	grey( true, true, DyeColor.GRAY, MapColor.COLOR_GRAY ),
	silver( true, true, DyeColor.LIGHT_GRAY, MapColor.COLOR_LIGHT_GRAY ),
	white( true, true, DyeColor.WHITE, MapColor.SNOW ),

	// colors...
	red( true, false, DyeColor.RED, MapColor.COLOR_RED ),
	orange( true, false, DyeColor.ORANGE, MapColor.COLOR_ORANGE ),
	yellow( true, false, DyeColor.YELLOW, MapColor.COLOR_YELLOW ),
	lime( true, false, DyeColor.LIME, MapColor.COLOR_LIGHT_GREEN ),
	green( true, false, DyeColor.GREEN, MapColor.COLOR_GREEN ),
	emerald( true, false, DyeColor.GREEN, DyeColor.CYAN, MapColor.COLOR_GREEN ),
	cyan( true, false, DyeColor.CYAN, MapColor.COLOR_CYAN ),
	azure( true, false, DyeColor.BLUE, DyeColor.CYAN, MapColor.COLOR_LIGHT_BLUE ),
	blue( true, false, DyeColor.BLUE, MapColor.COLOR_BLUE ),
	violet( true, false, DyeColor.PURPLE, MapColor.COLOR_PURPLE ),
	magenta( true, false, DyeColor.MAGENTA, MapColor.COLOR_MAGENTA ),
	pink( true, false, DyeColor.PINK, MapColor.COLOR_PINK ),

	// color modifiers
	dark( false, false, DyeColor.BLACK, MapColor.COLOR_BLACK ),
	light( false, false, DyeColor.WHITE, MapColor.SNOW );

	// description of characteristic
	public final boolean isModifier;
	public final boolean isSaturated;

	// dye information
	public final DyeColor primaryDye;
	public final DyeColor secondaryDye;

	// map color
	public final MapColor mapColor;

	EnumFlatColorAttributes(
			final boolean isColor,
			final boolean isSaturated,
			final DyeColor dye1,
			final DyeColor dye2,
			final MapColor mapColor )
	{
		isModifier = !isColor;
		this.isSaturated = isSaturated;
		primaryDye = dye1;
		secondaryDye = dye2;
		this.mapColor = mapColor;
	}

	EnumFlatColorAttributes(
			final boolean isColor,
			final boolean isSaturated,
			final DyeColor dye,
			final MapColor mapColor )
	{
		isModifier = !isColor;
		this.isSaturated = isSaturated;
		primaryDye = dye;
		secondaryDye = dye;
		this.mapColor = mapColor;
	}

	public static DyeColor getAlternateDye(
			final Set<EnumFlatColorAttributes> characteristics )
	{
		if ( characteristics.contains( EnumFlatColorAttributes.orange ) && characteristics.contains( EnumFlatColorAttributes.dark ) )
		{
			return DyeColor.BROWN;
		}

		if ( characteristics.contains( EnumFlatColorAttributes.blue ) && characteristics.contains( EnumFlatColorAttributes.light ) )
		{
			return DyeColor.LIGHT_BLUE;
		}

		return null;
	}

}
