package cz.yorick.ui.storage;

import cz.yorick.command.UiCommand;
import cz.yorick.util.Executable;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class UiStorage {
    private final SimpleGui gui;
    private final SimpleInventory inventory;
    private final ServerPlayerEntity player;
    private final DataSaver dataSaver;
    private final Executable onClose;
    private final Consumer<NbtCompound> nbtWriteCallback;
    private final Consumer<NbtCompound> nbtReadCallback;
    private final Map<ScreenProperty, Integer> properties = new HashMap<>();
    public UiStorage(SimpleGui gui, boolean invertSlotSelection, List<Integer> slots, DataSaver dataSaver, Executable onClose, NbtCompound argumentNbt, Consumer<NbtCompound> nbtWriteCallback, Consumer<NbtCompound> nbtReadCallback) {
        int slotSize = invertSlotSelection ? gui.getSize() - slots.size() : slots.size();
        this.gui = gui;
        this.inventory = getInventory(slotSize, dataSaver);
        this.player = gui.getPlayer();
        this.dataSaver = dataSaver;
        this.onClose = onClose;
        this.nbtWriteCallback = nbtWriteCallback;
        this.nbtReadCallback = nbtReadCallback;

        //inverted means that the specified slots are the locked slots
        if(invertSlotSelection) {
            //go through the inventory, ignore all slots present in the blacklist
            for (int i = 0; i < this.inventory.size(); i++) {
                if(!slots.contains(i)) {
                    gui.setSlotRedirect(i, new Slot(this.inventory, i, 0, 0));
                }
            }
        } else {
            //go through the whitelist and change all present slots
            for (int i = 0; i < slots.size(); i++) {
                gui.setSlotRedirect(slots.get(i), new Slot(this.inventory, i, 0, 0));
            }
        }

        //if there is a valid data saver, use its data instead of the argument
        if(this.dataSaver != null) {
            readNbt(this.dataSaver.loadFromStorage(this.player.getServer().getDataCommandStorage()));
            return;
        }

        //else default to the provided arguments
        if(argumentNbt != null) {
            readNbt(argumentNbt);
        }
    }

    private SimpleInventory getInventory(int size, DataSaver dataSaver) {
        if(size <= 0) {
            return null;
        }

        SimpleInventory inventory = new SimpleInventory(size);
        if(dataSaver != null && dataSaver.storeOnUpdate()) {
            inventory.addListener(updatedInventory -> this.save());
        }

        return inventory;
    }

    public void postOpen() {
        this.properties.forEach(this.gui::sendProperty);
    }

    public void onValidPropertySend(ScreenProperty property, int value) {
        this.properties.put(property, value);
    }

    public void onClose() {
        //no function and no data saver = no point in serializing nbt
        if(this.onClose == null && this.dataSaver == null) {
            return;
        }

        NbtCompound nbt = save();
        if(this.onClose != null) {
            this.onClose.execute(this.player.getCommandSource(), nbt);
        }
    }

    public int getProperty(ScreenProperty property) {
        return this.properties.getOrDefault(property, 0);
    }

    private NbtCompound save() {
        NbtCompound nbt = new NbtCompound();
        if(this.inventory != null) {
            Inventories.writeNbt(nbt, this.inventory.heldStacks, this.player.getRegistryManager());
        }

        //write extra or modify data with the callback (anvil text input, merchant trades)
        this.nbtWriteCallback.accept(nbt);
        //save the properties
        if(this.properties.size() > 0) {
            NbtCompound propertyNbt = new NbtCompound();
            this.properties.forEach((property, value) -> propertyNbt.putInt(property.name().toLowerCase(), value));
            nbt.put("properties", propertyNbt);
        }

        if(this.dataSaver != null) {
            this.dataSaver.writeToStorage(this.player.getServer().getDataCommandStorage(), nbt);
        }

        return nbt;
    }

    private void readNbt(NbtCompound nbt) {
        this.nbtReadCallback.accept(nbt);
        if(this.inventory != null) {
            Inventories.readNbt(nbt, this.inventory.heldStacks, player.getRegistryManager());
        }

        nbt.getCompound("properties").ifPresent(propertyNbt -> {
            for (String key : propertyNbt.getKeys()) {
                ScreenProperty property = UiCommand.getScreenProperty(key);
                //invalid property, pass
                if(property == null || !property.validFor(this.gui.getType())) {
                    continue;
                }

                //the key should be always present
                this.properties.put(property, propertyNbt.getInt(key, 0));
            }
        });
    }

    public interface Holder {
        UiStorage getStorage();
    }
}
