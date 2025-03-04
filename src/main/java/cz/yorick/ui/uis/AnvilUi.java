package cz.yorick.ui.uis;

import cz.yorick.ui.storage.UiStorage;
import cz.yorick.ui.templates.AnvilUiTemplate;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class AnvilUi extends AnvilInputGui implements UiStorage.Holder {
    private final UiStorage storage;
    public AnvilUi(AnvilUiTemplate template, ServerPlayerEntity player, NbtCompound nbtArgument) {
        super(player, template.includesPlayerInventory());
        this.storage = template.storage.create(template, this, nbtArgument, this::writeInput, this::readInput);
    }


    @Override
    public boolean open() {
        boolean open = super.open();
        this.storage.postOpen();
        return open;
    }

    @Override
    public void sendProperty(ScreenProperty property, int value) {
        super.sendProperty(property, value);
        this.storage.onValidPropertySend(property, value);
    }

    @Override
    public void onClose() {
        this.storage.onClose();
    }

    private void writeInput(NbtCompound nbt) {
        nbt.putString("input", this.getInput());
    }

    private void readInput(NbtCompound nbt) {
        this.setDefaultInputValue(nbt.getString("input"));
    }

    @Override
    public UiStorage getStorage() {
        return this.storage;
    }
}
