package cz.yorick.element.elements;

import cz.yorick.element.PersonalizedText;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;

import java.util.List;

public class StaticUiElement extends GuiElement implements RefreshableElement {
    private final ItemStack original;
    private final PersonalizedText name;
    private final List<PersonalizedText> lore;
    public StaticUiElement(ItemStack item, PersonalizedText name, List<PersonalizedText> lore, ClickCallback callback) {
        super(item, callback);
        this.original = item.copy();
        this.name = name;
        this.lore = lore;
    }

    //when added to an ui, replace the text with players placeholders
    @Override
    public void onAdded(SlotGuiInterface gui) {
        refresh(gui);
    }

    @Override
    public void refresh(SlotGuiInterface gui) {
        this.item = PersonalizedText.personalizeStack(gui.getPlayer(), this.original, this.name, this.lore);
    }
}
