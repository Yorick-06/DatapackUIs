package cz.yorick;

import cz.yorick.command.UiCommand;
import cz.yorick.placeholders.AttributePlaceholder;
import cz.yorick.placeholders.ScorePlaceholder;
import cz.yorick.placeholders.TagPlaceholder;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatapackUIs implements ModInitializer {
	public static final String MOD_ID = "datapack-uis";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UiRegistries.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> UiCommand.init(dispatcher));
		Placeholders.register(Identifier.of(MOD_ID, "score"), new ScorePlaceholder());
		Placeholders.register(Identifier.of(MOD_ID, "attribute"), new AttributePlaceholder());
		Placeholders.register(Identifier.of(MOD_ID, "tag"), new TagPlaceholder(true));
		Placeholders.register(Identifier.of(MOD_ID, "no_tag"), new TagPlaceholder(false));
	}
}