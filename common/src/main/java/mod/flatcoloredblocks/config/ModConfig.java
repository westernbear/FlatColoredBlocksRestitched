package mod.flatcoloredblocks.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConfig
{
	private static final Logger LOGGER = LoggerFactory.getLogger("FlatColoredBlocks/Config");
	private static final int MAX_BLOCK_STATES = 8192;

	private final Path myPath;

	// not configured..
	public int LAST_MAX_SHADES;

	@Configured( category = "Crafing" )
	public String solidCraftingBlock;

	@Configured( category = "Crafing" )
	public String transparentCraftingBlock;

	@Configured( category = "Crafing" )
	public String glowingCraftingBlock;

	@Configured( category = "Crafing" )
	public int solidCraftingOutput;

	@Configured( category = "Crafing" )
	public int transparentCraftingOutput;

	@Configured( category = "Crafing" )
	public int glowingCraftingOutput;

	@Configured( category = "Crafing" )
	public boolean allowCraftingTable;

	@Configured( category = "Integration" )
	public boolean ShowBlocksInJEI;

	@Configured( category = "Saturation" )
	public int SATURATION_SHADES;

	@Configured( category = "Saturation" )
	public double SATURATION_RANGE_EXPONENT;

	@Configured( category = "Saturation" )
	public double SATURATION_MIN;

	@Configured( category = "Saturation" )
	public double SATURATION_MAX;

	@Configured( category = "Saturation" )
	public int SATURATION_SHADES_GLOWING;

	@Configured( category = "Saturation" )
	public double SATURATION_RANGE_EXPONENT_GLOWING;

	@Configured( category = "Saturation" )
	public double SATURATION_MIN_GLOWING;

	@Configured( category = "Saturation" )
	public double SATURATION_MAX_GLOWING;

	@Configured( category = "Saturation" )
	public int SATURATION_SHADES_TRANSPARENT;

	@Configured( category = "Saturation" )
	public double SATURATION_RANGE_EXPONENT_TRANSPARENT;

	@Configured( category = "Saturation" )
	public double SATURATION_MIN_TRANSPARENT;

	@Configured( category = "Saturation" )
	public double SATURATION_MAX_TRANSPARENT;

	@Configured( category = "Hue" )
	public int HUE_SHADES;

	@Configured( category = "Hue" )
	public double HUE_RANGE_EXPONENT;

	@Configured( category = "Hue" )
	public double HUE_MIN;

	@Configured( category = "Hue" )
	public double HUE_MAX;

	@Configured( category = "Hue" )
	public int HUE_SHADES_GLOWING;

	@Configured( category = "Hue" )
	public double HUE_RANGE_EXPONENT_GLOWING;

	@Configured( category = "Hue" )
	public double HUE_MIN_GLOWING;

	@Configured( category = "Hue" )
	public double HUE_MAX_GLOWING;

	@Configured( category = "Hue" )
	public int HUE_SHADES_TRANSPARENT;

	@Configured( category = "Hue" )
	public double HUE_RANGE_EXPONENT_TRANSPARENT;

	@Configured( category = "Hue" )
	public double HUE_MIN_TRANSPARENT;

	@Configured( category = "Hue" )
	public double HUE_MAX_TRANSPARENT;

	@Configured( category = "Value" )
	public int VALUE_SHADES;

	@Configured( category = "Value" )
	public double VALUE_RANGE_EXPONENT;

	@Configured( category = "Value" )
	public double VALUE_MIN;

	@Configured( category = "Value" )
	public double VALUE_MAX;

	@Configured( category = "Value" )
	public int VALUE_SHADES_GLOWING;

	@Configured( category = "Value" )
	public double VALUE_RANGE_EXPONENT_GLOWING;

	@Configured( category = "Value" )
	public double VALUE_MIN_GLOWING;

	@Configured( category = "Value" )
	public double VALUE_MAX_GLOWING;

	@Configured( category = "Value" )
	public int VALUE_SHADES_TRANSPARENT;

	@Configured( category = "Value" )
	public double VALUE_RANGE_EXPONENT_TRANSPARENT;

	@Configured( category = "Value" )
	public double VALUE_MIN_TRANSPARENT;

	@Configured( category = "Value" )
	public double VALUE_MAX_TRANSPARENT;

	@Configured( category = "Glowing" )
	public boolean GLOWING_EMITS_LIGHT;

	@Configured( category = "Glowing" )
	public int GLOWING_SHADES;

	@Configured( category = "Glowing" )
	public double GLOWING_RANGE_EXPONENT;

	@Configured( category = "Glowing" )
	public double GLOWING_MIN;

	@Configured( category = "Glowing" )
	public double GLOWING_MAX;

	@Configured( category = "Transparency" )
	public int TRANSPARENCY_SHADES;

	@Configured( category = "Transparency" )
	public double TRANSPARENCY_RANGE_EXPONENT;

	@Configured( category = "Transparency" )
	public double TRANSPARENCY_MIN;

	@Configured( category = "Transparency" )
	public double TRANSPARENCY_MAX;

	@Configured( category = "Texture" )
	public EnumFlatBlockTextures DISPLAY_TEXTURE;

	@Configured( category = "Texture" )
	public EnumFlatBlockTextures DISPLAY_TEXTURE_GLOWING;

	@Configured( category = "Texture" )
	public EnumFlatTransparentBlockTextures DISPLAY_TEXTURE_TRANSPARENT;

	@Configured( category = "Client Settings" )
	public boolean showHSV;

	@Configured( category = "Client Settings" )
	public boolean showRGB;

	@Configured( category = "Client Settings" )
	public boolean showHEX;

	@Configured( category = "Client Settings" )
	public boolean showLight;

	@Configured( category = "Client Settings" )
	public boolean showOpacity;

	void setDefaults()
	{
		solidCraftingBlock = "flatcoloredblocks:solid_crafting_block";
		transparentCraftingBlock = "flatcoloredblocks:transparent_crafting_block";
		glowingCraftingBlock = "flatcoloredblocks:glowing_crafting_block";
		solidCraftingOutput = 1;
		transparentCraftingOutput = 1;
		glowingCraftingOutput = 1;
		allowCraftingTable = true;
		showHEX = showHSV = showRGB = showLight = showOpacity = true;

		LAST_MAX_SHADES = 0;

		// shades of 4th dimension
		GLOWING_SHADES = 1;
		GLOWING_RANGE_EXPONENT = 1.0;
		GLOWING_MIN = 1.0;
		GLOWING_MAX = 1.0;
		GLOWING_EMITS_LIGHT = true;

		TRANSPARENCY_SHADES = 1;
		TRANSPARENCY_RANGE_EXPONENT = 1.0;
		TRANSPARENCY_MIN = 0.5;
		TRANSPARENCY_MAX = 0.5;

		// normal
		HUE_SHADES = 32;
		SATURATION_SHADES = 4;
		VALUE_SHADES = 10;

		SATURATION_RANGE_EXPONENT = 0.9;
		SATURATION_MIN = 0.2;
		SATURATION_MAX = 1.0;

		HUE_RANGE_EXPONENT = 1.0;
		HUE_MIN = 0.0;
		HUE_MAX = 0.96;

		VALUE_RANGE_EXPONENT = 1.0;
		VALUE_MIN = 0.2;
		VALUE_MAX = 1.0;

		// glowing
		HUE_SHADES_GLOWING = 32;
		SATURATION_SHADES_GLOWING = 4;
		VALUE_SHADES_GLOWING = 10;

		SATURATION_RANGE_EXPONENT_GLOWING = 0.9;
		SATURATION_MIN_GLOWING = 0.2;
		SATURATION_MAX_GLOWING = 1.0;

		HUE_RANGE_EXPONENT_GLOWING = 1.0;
		HUE_MIN_GLOWING = 0.0;
		HUE_MAX_GLOWING = 0.96;

		VALUE_RANGE_EXPONENT_GLOWING = 1.0;
		VALUE_MIN_GLOWING = 0.2;
		VALUE_MAX_GLOWING = 1.0;

		// transparent
		HUE_SHADES_TRANSPARENT = 32;
		SATURATION_SHADES_TRANSPARENT = 4;
		VALUE_SHADES_TRANSPARENT = 10;

		SATURATION_RANGE_EXPONENT_TRANSPARENT = 0.9;
		SATURATION_MIN_TRANSPARENT = 0.2;
		SATURATION_MAX_TRANSPARENT = 1.0;

		HUE_RANGE_EXPONENT_TRANSPARENT = 1.0;
		HUE_MIN_TRANSPARENT = 0.0;
		HUE_MAX_TRANSPARENT = 0.96;

		VALUE_RANGE_EXPONENT_TRANSPARENT = 1.0;
		VALUE_MIN_TRANSPARENT = 0.2;
		VALUE_MAX_TRANSPARENT = 1.0;

		// textures
		DISPLAY_TEXTURE = EnumFlatBlockTextures.DRYWALL;
		DISPLAY_TEXTURE_GLOWING = EnumFlatBlockTextures.PULSE;
		DISPLAY_TEXTURE_TRANSPARENT = EnumFlatTransparentBlockTextures.SEMI_GLASS;

		// Integration
		ShowBlocksInJEI = false;
	}

	public ModConfig(final Path path)
	{
		myPath = path;
		setDefaults();
		load();
		validate();
		save();
	}

	public Path getFilePath()
	{
		return myPath;
	}

	public void updateLastMaxShades()
	{
		LAST_MAX_SHADES = Math.max(
				shadeCount(HUE_SHADES, SATURATION_SHADES, VALUE_SHADES),
				Math.max(
						shadeCount(HUE_SHADES_GLOWING, SATURATION_SHADES_GLOWING, VALUE_SHADES_GLOWING),
						shadeCount(HUE_SHADES_TRANSPARENT, SATURATION_SHADES_TRANSPARENT, VALUE_SHADES_TRANSPARENT)));
	}

	public void save()
	{
		Properties values = new Properties();
		for (Field field : configuredFields())
		{
			try
			{
				values.setProperty(field.getName(), String.valueOf(field.get(this)));
			}
			catch (IllegalAccessException e)
			{
				throw new IllegalStateException("Unable to read config field " + field.getName(), e);
			}
		}
		values.setProperty("_LAST_MAX_SHADES", Integer.toString(LAST_MAX_SHADES));

		Path parent = myPath.getParent();
		Path temporary = myPath.resolveSibling(myPath.getFileName() + ".tmp");
		try
		{
			if (parent != null)
			{
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8))
			{
				values.store(writer, "Flat Colored Blocks Restitched");
			}
			try
			{
				Files.move(temporary, myPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (AtomicMoveNotSupportedException ignored)
			{
				Files.move(temporary, myPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e)
		{
			LOGGER.error("Unable to save {}", myPath, e);
		}
	}

	public List<Field> configuredFields()
	{
		return Arrays.stream(getClass().getFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.filter(field -> field.isAnnotationPresent(Configured.class))
				.sorted(Comparator.comparing(Field::getName))
				.toList();
	}

	private void load()
	{
		if (!Files.isRegularFile(myPath))
		{
			return;
		}

		Properties values = new Properties();
		try (Reader reader = Files.newBufferedReader(myPath, StandardCharsets.UTF_8))
		{
			values.load(reader);
		}
		catch (IOException e)
		{
			LOGGER.warn("Unable to read {}, using defaults", myPath, e);
			return;
		}

		for (Field field : configuredFields())
		{
			String value = values.getProperty(field.getName());
			if (value == null)
			{
				continue;
			}
			try
			{
				field.set(this, parse(field.getType(), value));
			}
			catch (ReflectiveOperationException | IllegalArgumentException e)
			{
				LOGGER.warn("Ignoring invalid {}={} in {}", field.getName(), value, myPath);
			}
		}

		try
		{
			LAST_MAX_SHADES = Integer.parseInt(values.getProperty("_LAST_MAX_SHADES", "0"));
		}
		catch (NumberFormatException ignored)
		{
			LAST_MAX_SHADES = 0;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Object parse(Class<?> type, String value)
	{
		if (type == String.class)
		{
			return value.trim();
		}
		if (type == int.class)
		{
			return Integer.parseInt(value.trim());
		}
		if (type == double.class)
		{
			double parsed = Double.parseDouble(value.trim());
			if (!Double.isFinite(parsed))
			{
				throw new IllegalArgumentException("non-finite number");
			}
			return parsed;
		}
		if (type == boolean.class)
		{
			String parsed = value.trim();
			if (!parsed.equalsIgnoreCase("true") && !parsed.equalsIgnoreCase("false"))
			{
				throw new IllegalArgumentException("not a boolean");
			}
			return Boolean.parseBoolean(parsed);
		}
		if (type.isEnum())
		{
			return Enum.valueOf((Class<? extends Enum>) type, value.trim().toUpperCase());
		}
		throw new IllegalArgumentException("unsupported config type " + type);
	}

	private void validate()
	{
		solidCraftingBlock = validIdentifier(solidCraftingBlock, "flatcoloredblocks:solid_crafting_block");
		transparentCraftingBlock = validIdentifier(transparentCraftingBlock, "flatcoloredblocks:transparent_crafting_block");
		glowingCraftingBlock = validIdentifier(glowingCraftingBlock, "flatcoloredblocks:glowing_crafting_block");
		solidCraftingOutput = Math.clamp(solidCraftingOutput, 1, 64);
		transparentCraftingOutput = Math.clamp(transparentCraftingOutput, 1, 64);
		glowingCraftingOutput = Math.clamp(glowingCraftingOutput, 1, 64);

		HUE_SHADES = positive(HUE_SHADES, 32);
		SATURATION_SHADES = positive(SATURATION_SHADES, 4);
		VALUE_SHADES = positive(VALUE_SHADES, 10);
		HUE_SHADES_GLOWING = positive(HUE_SHADES_GLOWING, 32);
		SATURATION_SHADES_GLOWING = positive(SATURATION_SHADES_GLOWING, 4);
		VALUE_SHADES_GLOWING = positive(VALUE_SHADES_GLOWING, 10);
		HUE_SHADES_TRANSPARENT = positive(HUE_SHADES_TRANSPARENT, 32);
		SATURATION_SHADES_TRANSPARENT = positive(SATURATION_SHADES_TRANSPARENT, 4);
		VALUE_SHADES_TRANSPARENT = positive(VALUE_SHADES_TRANSPARENT, 10);

		if (shadeCount(HUE_SHADES, SATURATION_SHADES, VALUE_SHADES) > MAX_BLOCK_STATES)
		{
			HUE_SHADES = 32;
			SATURATION_SHADES = 4;
			VALUE_SHADES = 10;
		}
		if (shadeCount(HUE_SHADES_GLOWING, SATURATION_SHADES_GLOWING, VALUE_SHADES_GLOWING) > MAX_BLOCK_STATES)
		{
			HUE_SHADES_GLOWING = 32;
			SATURATION_SHADES_GLOWING = 4;
			VALUE_SHADES_GLOWING = 10;
		}
		if (shadeCount(HUE_SHADES_TRANSPARENT, SATURATION_SHADES_TRANSPARENT, VALUE_SHADES_TRANSPARENT) > MAX_BLOCK_STATES)
		{
			HUE_SHADES_TRANSPARENT = 32;
			SATURATION_SHADES_TRANSPARENT = 4;
			VALUE_SHADES_TRANSPARENT = 10;
		}

		GLOWING_SHADES = uniqueVariantCount(GLOWING_SHADES, GLOWING_RANGE_EXPONENT, GLOWING_MIN, GLOWING_MAX);
		TRANSPARENCY_SHADES = uniqueVariantCount(TRANSPARENCY_SHADES, TRANSPARENCY_RANGE_EXPONENT, TRANSPARENCY_MIN, TRANSPARENCY_MAX);
		updateLastMaxShades();
	}

	private static int positive(int value, int fallback)
	{
		return value > 0 ? value : fallback;
	}

	private static int shadeCount(int hue, int saturation, int value)
	{
		try
		{
			return Math.addExact(Math.multiplyExact(Math.multiplyExact(hue, saturation), value), value);
		}
		catch (ArithmeticException ignored)
		{
			return Integer.MAX_VALUE;
		}
	}

	private static String validIdentifier(String value, String fallback)
	{
		return Identifier.tryParse(value) == null ? fallback : value;
	}

	private static int uniqueVariantCount(int count, double exponent, double min, double max)
	{
		count = Math.clamp(count, 1, 16);
		Set<Integer> ids = new HashSet<>();
		for (int index = 0; index < count; index++)
		{
			double position = count == 1 ? 0 : index / (double) (count - 1);
			double adjusted = Math.pow(position, exponent);
			int id = Math.clamp((int) (255 * (adjusted * max + min * (1 - adjusted))), 0, 255);
			if (!ids.add(id))
			{
				LOGGER.warn("Variant range creates duplicate block IDs; using one variant");
				return 1;
			}
		}
		return count;

	}

}
