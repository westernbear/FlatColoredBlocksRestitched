package mod.flatcoloredblocks.client;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ClientPlatform {
	private ClientPlatform() {}

	@ExpectPlatform
	public static void registerColorsAndModels() {
		throw new AssertionError();
	}
}
