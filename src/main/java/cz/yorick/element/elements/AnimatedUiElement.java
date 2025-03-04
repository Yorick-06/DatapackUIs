package cz.yorick.element.elements;

import cz.yorick.element.PersonalizedText;
import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AnimatedUiElement extends AnimatedGuiElement implements RefreshableElement {
    private final List<ItemStack> originals;
    private final PersonalizedText name;
    private final List<PersonalizedText> lore;
    public AnimatedUiElement(List<ItemStack> items, int interval, boolean random, PersonalizedText name, List<PersonalizedText> lore, ClickCallback callback) {
        super(items.toArray(new ItemStack[0]), interval, random, callback);
        this.originals = items.stream().map(ItemStack::copy).toList();
        this.name = name;
        this.lore = lore;
    }

    //when added to an ui, replace the text on all items with players placeholders
    @Override
    public void onAdded(SlotGuiInterface gui) {
        refresh(gui);
    }

    @Override
    public void refresh(SlotGuiInterface gui) {
        for (int i = 0; i < this.originals.size(); i++) {
            this.items[i] = PersonalizedText.personalizeStack(gui.getPlayer(), this.originals.get(i), this.name, this.lore);
        }
    }
}
