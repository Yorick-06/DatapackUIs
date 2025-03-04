package cz.yorick.element.templates;

import com.mojang.serialization.MapCodec;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.api.codec.CodecUtils;
import cz.yorick.api.codec.annotations.FieldId;
import cz.yorick.api.codec.annotations.IncludeParent;
import cz.yorick.api.codec.annotations.OptionalField;
import cz.yorick.element.Displayed;
import cz.yorick.element.ElementTemplate;
import cz.yorick.element.PersonalizedText;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.function.BiConsumer;

@IncludeParent
public class ToggleableElementTemplate extends ElementTemplate {
    public static final MapCodec<ToggleableElementTemplate> CODEC = addCodecs(ClassFieldsCodec.builder(ToggleableElementTemplate.class))
            .withCodec(Displayed.CODEC, "enabled_display")
            .withCodec(PersonalizedText.CODEC, "enabled_name")
            .withCodec(PersonalizedText.CODEC.listOf(), "enabled_lore")
            .withCodec(CodecUtils.caseConvertingEnum(ClickType.class), "toggle_button")
            .buildMap();

    @OptionalField
    @FieldId(id = "enabled_display")
    private final Displayed enabledDisplay = null;
    @OptionalField
    @FieldId(id = "enabled_name")
    private final PersonalizedText enabledName = null;
    @OptionalField
    @FieldId(id = "enabled_lore")
    private final List<PersonalizedText> enabledLore = null;

    @OptionalField
    @FieldId(id = "toggle_button")
    private final ClickType toggleButton = null;
    private String tag = "toggled";

    @Override
    public GuiElementInterface createNew(ServerPlayerEntity player) {
        GuiElementInterface[] elements = new GuiElementInterface[2];

        GuiElementInterface.ClickCallback setDisabled = getOnClick(this.onClick, (gui, index) -> {
            gui.setSlot(index, elements[0]);
            gui.getPlayer().getCommandTags().remove(this.tag);
        });
        GuiElementInterface.ClickCallback setEnabled = getOnClick(this.onClick, (gui, index) -> {
            gui.setSlot(index, elements[1]);
            gui.getPlayer().getCommandTags().add(this.tag);
        });

        elements[0] = this.display.create(this.name, this.lore, setEnabled);
        //create the enabled with the provided values, but default to the disabled ones if missing
        Displayed enabledDisplay = this.enabledDisplay != null ? this.enabledDisplay : this.display;
        PersonalizedText enabledName = this.enabledName != null ? this.enabledName : this.name;
        List<PersonalizedText> enabledLore = this.enabledLore != null ? this.enabledLore : this.lore;
        elements[1] = enabledDisplay.create(enabledName, enabledLore, setDisabled);

        return player.getCommandTags().contains(this.tag) ? elements[1] : elements[0];
    }

    private GuiElementInterface.ClickCallback getOnClick(GuiElementInterface.ClickCallback original, BiConsumer<SlotGuiInterface, Integer> elementSwitcher) {
        //if no button is specified, all buttons activate the toggle feature
        if(this.toggleButton == null) {
            return (index, type, action, gui) -> {
                elementSwitcher.accept(gui, index);
                original.click(index, type, action, gui);
            };
        }

        //if a button is specified, switch only on that button
        return (index, type, action, gui) -> {
            if(type == this.toggleButton) {
                elementSwitcher.accept(gui, index);
            }

            original.click(index, type, action, gui);
        };
    }

    @Override
    public MapCodec<? extends ElementTemplate> getCodec() {
        return CODEC;
    }
}
