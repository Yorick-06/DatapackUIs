package cz.yorick.element.elements;

import cz.yorick.element.ElementTemplate;
import cz.yorick.element.templates.DynamicElementTemplate;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;

public class DynamicUiElement implements GuiElementInterface, RefreshableElement {
    private final DynamicElementTemplate dynamicTemplate;
    private ElementTemplate displayedTemplate;
    private GuiElementInterface element;
    public DynamicUiElement(DynamicElementTemplate template, GuiElementInterface element) {
        this.dynamicTemplate = template;
        this.element = element;
    }

    @Override
    public void refresh(SlotGuiInterface gui) {
        ElementTemplate newTemplate = this.dynamicTemplate.getTemplate(gui.getPlayer());
        //if the template is the same, try to refresh the element
        if(newTemplate == this.displayedTemplate) {
            if(this.element instanceof RefreshableElement refreshableElement) {
                refreshableElement.refresh(gui);
            }
            return;
        }

        //try to replace the whole element
        if(newTemplate == null) {
            //if the new template is null, create the default
            this.element = this.dynamicTemplate.createDefault();
            return;
        }

        this.element = newTemplate.createNew(gui.getPlayer());
        this.displayedTemplate = newTemplate;
    }

    @Override
    public ItemStack getItemStack() {
        return this.element.getItemStack();
    }

    public ClickCallback getGuiCallback() {
        return this.element.getGuiCallback();
    }

    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        return this.element.getItemStackForDisplay(gui);
    }

    public void onAdded(SlotGuiInterface gui) {
        refresh(gui);
        this.element.onAdded(gui);
    }

    public void onRemoved(SlotGuiInterface gui) {
        this.element.onRemoved(gui);
    }
}
