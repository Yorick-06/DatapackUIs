package cz.yorick.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import cz.yorick.DatapackUIs;
import cz.yorick.UiRegistries;
import cz.yorick.api.resources.ResourceUtil;
import cz.yorick.ui.UiTemplate;
import cz.yorick.ui.storage.UiStorage;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UiCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ui").requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .then(CommandManager.literal("open")
                    .then(CommandManager.argument("ui", IdentifierArgumentType.identifier()).suggests(UiCommand::suggestUi)
                        .then(CommandManager.argument("data", NbtCompoundArgumentType.nbtCompound())
                            .executes(context -> openUi(context.getSource(), EntityArgumentType.getPlayer(context, "player"), IdentifierArgumentType.getIdentifier(context, "ui"), NbtCompoundArgumentType.getNbtCompound(context, "data")))
                        ).executes(context -> openUi(context.getSource(), EntityArgumentType.getPlayer(context, "player"), IdentifierArgumentType.getIdentifier(context, "ui"), null))
                    )
                ).then(CommandManager.literal("close")
                    .executes(context -> closeUi(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                ).then(CommandManager.literal("property")
                    .then(CommandManager.argument("property", StringArgumentType.string()).suggests(UiCommand::suggestProperty)
                        .then(CommandManager.literal("set")
                            .then(CommandManager.argument("value", IntegerArgumentType.integer())
                                .executes(context -> setProperty(context.getSource(), EntityArgumentType.getPlayer(context, "player"), StringArgumentType.getString(context, "property"), IntegerArgumentType.getInteger(context, "value")))
                            )
                        ).then(CommandManager.literal("get")
                            .executes(context -> getProperty(context.getSource(), EntityArgumentType.getPlayer(context, "player"), StringArgumentType.getString(context, "property")))
                        )
                    )
                )
            )
        );
    }

    private static CompletableFuture<Suggestions> suggestUi(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(UiRegistries.UI_REGISTRY.getKeys(), builder);
    }

    private static int openUi(ServerCommandSource source, ServerPlayerEntity player, Identifier identifier, NbtCompound data) {
        try {
            UiTemplate template = UiRegistries.UI_REGISTRY.getOrNull(identifier);
            if(template == null) {
                source.sendError(Text.literal("Unknown UI '" + identifier + "'"));
                return 0;
            }

            template.openFor(player, data);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            DatapackUIs.LOGGER.error("Could not open ui: " + identifier, e);
            return 0;
        }
    }

    private static int closeUi(ServerCommandSource source, ServerPlayerEntity player) {
        GuiInterface gui = GuiHelpers.getCurrentGui(player);
        if(gui == null) {
            source.sendError(Text.literal("The player does not have a UI opened or the UI is not provided by this mod"));
            return 0;
        }

        gui.close();
        source.sendError(Text.literal("Closed " + player.getGameProfile().getName() + "'s UI"));
        return Command.SINGLE_SUCCESS;
    }

    private static final List<String> properties = Arrays.stream(ScreenProperty.values()).map(value -> value.name().toLowerCase()).toList();
    private static CompletableFuture<Suggestions> suggestProperty(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ResourceUtil.suggestMatching(properties, builder);
    }

    private static int setProperty(ServerCommandSource source, ServerPlayerEntity player, String property, int value) {
        ScreenProperty screenProperty = getScreenProperty(property);
        if(screenProperty == null) {
            source.sendError(Text.literal("Unknown UI property '" + property + "'"));
            return 0;
        }

        GuiInterface gui = GuiHelpers.getCurrentGui(player);
        if(gui == null) {
            source.sendError(Text.literal("The player does not have a UI opened or the UI is not provided by this mod"));
            return 0;
        }

        if(!setProperty(gui, screenProperty, value)) {
            source.sendError(Text.literal("Property '" + property + "' is invalid for handler type '" + Registries.SCREEN_HANDLER.getId(gui.getType()) + "'"));
            return 0;
        }
        
        source.sendMessage(Text.literal("Set the property " + screenProperty.name() + " of " + player.getGameProfile().getName() + " to " + value).formatted(Formatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    
    private static int getProperty(ServerCommandSource source, ServerPlayerEntity player, String property) {
        ScreenProperty screenProperty = getScreenProperty(property);
        if(screenProperty == null) {
            source.sendError(Text.literal("Unknown UI property '" + property + "'"));
            return 0;
        }

        GuiInterface gui = GuiHelpers.getCurrentGui(player);
        if((!(gui instanceof UiStorage.Holder))) {
            source.sendError(Text.literal("The player does not have a UI opened or the UI is not provided by this mod"));
            return 0;
        }

        if(!screenProperty.validFor(gui.getType())) {
            source.sendError(Text.literal("Property '" + property + "' is invalid for handler type '" + Registries.SCREEN_HANDLER.getId(gui.getType()) + "'"));
            return 0;
        }

        int value = ((UiStorage.Holder)gui).getStorage().getProperty(screenProperty);
        source.sendMessage(Text.literal("The value of the property " + screenProperty.name() + " of " + player.getGameProfile().getName() + "'s screen is " + value).formatted(Formatting.GREEN));
        return value;
    }

    public static ScreenProperty getScreenProperty(String id) {
        try {
            return ScreenProperty.valueOf(id.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean setProperty(GuiInterface gui, ScreenProperty property, int value) {
        try {
            gui.sendProperty(property, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
