package mod.flatcoloredblocks.api;

import java.util.Collection;
import java.util.Optional;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

/**
 * Public API for Flat Colored Blocks. Prefer this surface over internal classes.
 */
@ApiStatus.AvailableSince("8.2.0")
public interface FlatColoredBlocksAPI {
	static FlatColoredBlocksAPI getInstance() {
		return mod.flatcoloredblocks.api.impl.FlatColoredBlocksAPIImpl.INSTANCE;
	}

	String modId();

	Collection<ColorType> colorTypes();

	Optional<ColorType> colorType(Identifier id);

	Optional<ColorConfigView> configuration(ColorType type);

	Collection<BlockFlatColored> blocks();

	Optional<BlockFlatColored> firstBlock(ColorType type);

	int colorFromState(BlockState state);

	int shadeToRgb(BlockFlatColored block, int shade);

	/**
	 * Registers an additional color type and its block variants.
	 * Must be called during mod initialization before the palette freezes (Architectury {@code LifecycleEvent.SETUP}).
	 *
	 * @throws IllegalArgumentException if the id is already registered
	 * @throws IllegalStateException if registration is closed
	 */
	ColorType registerColorType(ColorTypeRegistration registration);

	void addBlockRegisteredListener(BlockRegisteredCallback callback);

	void addPaletteBuiltListener(PaletteBuiltCallback callback);

	void addCrafterOpenedListener(CrafterOpenedCallback callback);

	EnumFlatBlockType asBuiltinEnum(ColorType type);
}
