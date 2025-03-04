package cz.yorick.ui.templates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import cz.yorick.UiRegistries;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.api.codec.annotations.FieldId;
import cz.yorick.api.codec.annotations.IncludeParent;
import cz.yorick.api.codec.annotations.OptionalField;
import cz.yorick.element.ElementTemplate;
import cz.yorick.ui.UiTemplate;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

@IncludeParent
public class ChestContainerUiTemplate extends UiTemplate {
    private int rows = 6;
    @OptionalField
    @FieldId(id = "fill_element")
    private final ElementTemplate defaultFillElement = null;
    public static final MapCodec<ChestContainerUiTemplate> CODEC = addCodecsAndValidator(
            ClassFieldsCodec.builder(ChestContainerUiTemplate.class)
                    .withCodec(Codec.intRange(1, 6), "rows")
                    .withCodec(UiRegistries.ELEMENT_REGISTRY.getCodec(), "fill_element")
    ).buildMap();

    @Override
    public SimpleGui getNewGui(ServerPlayerEntity player, NbtCompound nbtArgument) {
        SimpleGui gui = super.getNewGui(player, nbtArgument);
        if(this.defaultFillElement != null) {
            for (int i = 0; i < gui.getSize(); i++) {
                gui.setSlot(i, this.defaultFillElement.createNew(player));
            }
        }
        return gui;
    }

    @Override
    public MapCodec<? extends UiTemplate> getCodec() {
        return CODEC;
    }

    @Override
    public ScreenHandlerType<?> getHandlerType() {
        return switch (this.rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> throw new IllegalArgumentException("Somehow got an invalid amount of rows (" + rows + "), should be verified by the codec!");
        };
    }
}
