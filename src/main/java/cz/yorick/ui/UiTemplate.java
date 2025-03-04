package cz.yorick.ui;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import cz.yorick.UiRegistries;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.api.codec.annotations.FieldId;
import cz.yorick.api.codec.annotations.OptionalField;
import cz.yorick.element.ElementTemplate;
import cz.yorick.element.PersonalizedText;
import cz.yorick.util.Executable;
import cz.yorick.ui.storage.DataSaver;
import cz.yorick.ui.storage.StorageFactory;
import cz.yorick.ui.uis.SimpleUi;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Map;

public abstract class UiTemplate {
    private final PersonalizedText title = PersonalizedText.of(Either.left("No title set"));
    @OptionalField
    protected final Map<Integer, ElementTemplate> elements = Map.of();
    @OptionalField
    @FieldId(id = "on_close")
    public final Executable onClose = null;
    @OptionalField
    @FieldId(id = "include_player_inventory")
    private boolean includePlayerInventory = false;
    @OptionalField
    @FieldId(id = "lock_player_inventory")
    private boolean lockPlayerInventory = true;
    @OptionalField
    public final StorageFactory storage = new StorageFactory(List.of(), false);
    @OptionalField
    public final Either<Boolean, DataSaver> save = Either.left(false);

    public boolean includesPlayerInventory() {
        return this.includePlayerInventory;
    }

    public abstract MapCodec<? extends UiTemplate> getCodec();

    public abstract ScreenHandlerType<?> getHandlerType();

    public SimpleGui getNewGui(ServerPlayerEntity player, NbtCompound nbtArgument) {
        return new SimpleUi(this, player, nbtArgument);
    }

    public void openFor(ServerPlayerEntity player, NbtCompound data) {
        SimpleGui gui = getNewGui(player, data);
        setGuiValues(gui);
        gui.open();
    }

    public void setGuiValues(SimpleGui gui) {
        this.elements.forEach((slot, elementFactory) -> gui.setSlot(slot, elementFactory.createNew(gui.getPlayer())));
        gui.setTitle(Placeholders.parseText(this.title.getFor(gui.getPlayer()), PlaceholderContext.of(gui.getPlayer())));
        gui.setLockPlayerInventory(this.lockPlayerInventory);
    }

    //only strings can be keys in a map
    private static final Codec<Integer> SLOT_ID = Codecs.NON_EMPTY_STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(Integer.valueOf(string));
        } catch (Exception e) {
            return DataResult.error(() -> "Could not parse '" + string + "': " + e.getMessage());
        }
    }, String::valueOf);

    public static final Codec<Map<Integer, ElementTemplate>> ELEMENTS_CODEC = Codec.unboundedMap(SLOT_ID, UiRegistries.ELEMENT_REGISTRY.getCodec());

    public static MapCodec<? extends UiTemplate> basicCodec(ScreenHandlerType<?> handlerType) {
        //i hate this
        MapCodec<? extends UiTemplate>[] codec = new MapCodec[1];
        MapCodec<? extends UiTemplate> mapCodec = addCodecsAndValidator(ClassFieldsCodec.builder(UiTemplate.class, () -> new UiTemplate() {
            @Override
            public MapCodec<? extends UiTemplate> getCodec() {
                return codec[0];
            }

            @Override
            public ScreenHandlerType<?> getHandlerType() {
                return handlerType;
            }
        })).buildMap();
        codec[0] = mapCodec;
        return mapCodec;
    }

    public static <C extends UiTemplate, T extends C> ClassFieldsCodec.Builder<C, T> addCodecsAndValidator(ClassFieldsCodec.Builder<C, T> builder) {
        return builder
                .withCodec(ELEMENTS_CODEC, "elements")
                .withCodec(Executable.CODEC, Executable.class)
                .withCodec(PersonalizedText.CODEC, "title")
                .withCodec(StorageFactory.CODEC, "storage")
                .withCodec(Codec.either(Codec.BOOL, DataSaver.CODEC), "save")
                .postProcessor(template -> {
                    int size = GuiHelpers.getHeight(template.getHandlerType()) * GuiHelpers.getWidth(template.getHandlerType()) + (template.includesPlayerInventory() ? 36 : 0);
                    for (Integer slot : template.elements.keySet()) {
                        if(slot >= size) {
                            return DataResult.error(() -> "The specified UI has a size of " + size + ", element in slot " + slot + " is out of bounds!");
                        }
                    }

                    for (Integer slot : template.storage.slots()) {
                        if(slot >= size) {
                            return DataResult.error(() -> "The specified UI has a size of " + size + ", slot " + slot + " is out of bounds!");
                        }
                    }

                    return DataResult.success(template);
                });
    }
}
