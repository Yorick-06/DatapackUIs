package cz.yorick.element;

import com.mojang.serialization.MapCodec;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.api.codec.annotations.FieldId;
import cz.yorick.api.codec.annotations.OptionalField;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public abstract class ElementTemplate {
    protected final Displayed display = new Displayed.Static(new ItemStack(Items.BARRIER));
    @OptionalField
    protected final PersonalizedText name = null;
    @OptionalField
    protected final List<PersonalizedText> lore = List.of();
    @OptionalField
    @FieldId(id = "on_click")
    protected final GuiElementInterface.ClickCallback onClick = GuiElementInterface.EMPTY_CALLBACK;

    public abstract GuiElementInterface createNew(ServerPlayerEntity player);

    public abstract MapCodec<? extends ElementTemplate> getCodec();

    public static <C, T extends C> ClassFieldsCodec.Builder<C, T> addCodecs(ClassFieldsCodec.Builder<C, T> builder) {
        return builder
                .withCodec(Displayed.CODEC, "display")
                .withCodec(PersonalizedText.CODEC, "name")
                .withCodec(PersonalizedText.CODEC.listOf(), "lore")
                .withCodec(ClickHandler.CODEC, "on_click");
    }
}
