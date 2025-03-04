package cz.yorick.ui.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.DatapackUIs;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record DataSaver(Identifier storage, String[] path, boolean storeOnUpdate) {
    public static final Codec<DataSaver> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("storage_id").forGetter(DataSaver::storage),
            Codecs.NON_EMPTY_STRING.optionalFieldOf("path", "").forGetter(storage -> String.join(".", storage.path())),
            Codec.BOOL.optionalFieldOf("store_on_update", true).forGetter(DataSaver::storeOnUpdate)
    ).apply(instance, DataSaver::of));

    public static DataSaver forPlayer(ServerPlayerEntity player, Identifier uiId, boolean storeOnUpdate) {
        return new DataSaver(Identifier.of(DatapackUIs.MOD_ID, player.getUuidAsString()), new String[]{uiId.toString()}, storeOnUpdate);
    }

    private static DataSaver of(Identifier storageId, String stringPath, boolean storeOnUpdate) {
        if(stringPath.equals("")) {
            return new DataSaver(storageId, new String[0], storeOnUpdate);
        }

        return new DataSaver(storageId, stringPath.split("\\."), storeOnUpdate);
    }

    public void save(DataCommandStorage dataStorage, NbtCompound nbt) {
        //if no path is set, just save everything directly
        if(this.path.length == 0) {
            dataStorage.set(this.storage, nbt);
            return;
        }

        NbtCompound savedNbt = dataStorage.get(this.storage);
        //go over the path, keeping the last path as the key to the data
        for (int i = 0; i < this.path.length - 1; i++) {
            savedNbt = savedNbt.getCompound(this.path[i]);
        }

        //set the data
        savedNbt.put(this.path[this.path.length - 1], nbt);

        //no idea if the data storage returns a reference or a copy so just set it
        dataStorage.set(this.storage, savedNbt);
    }

    public NbtCompound load(DataCommandStorage dataStorage) {
        //if no path is set, load everything directly
        if(this.path.length == 0) {
            return dataStorage.get(this.storage);
        }

        NbtCompound savedNbt = dataStorage.get(this.storage);
        for (String path : this.path) {
            savedNbt = savedNbt.getCompound(path);
        }

        return savedNbt;
    }
}
