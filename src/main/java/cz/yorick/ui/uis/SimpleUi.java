package cz.yorick.ui.uis;

import cz.yorick.ui.UiTemplate;
import cz.yorick.ui.storage.UiStorage;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class SimpleUi extends SimpleGui implements UiStorage.Holder {
    private final UiStorage storage;
    public SimpleUi(UiTemplate template, ServerPlayerEntity player, NbtCompound nbtArgument) {
        super(template.getHandlerType(), player, template.includesPlayerInventory());
        this.storage = template.storage.create(template, this, nbtArgument, nbt -> {}, nbt -> {});
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

    @Override
    public UiStorage getStorage() {
        return this.storage;
    }
}
