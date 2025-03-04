package cz.yorick.ui.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.api.codec.MappedAlternativeCodecs;
import cz.yorick.ui.UiTemplate;
import cz.yorick.UiRegistries;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.function.Consumer;

public record StorageFactory(List<Integer> slots, boolean invertSelection) {
    private static final Codec<StorageFactory> FACTORY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf().fieldOf("slots").forGetter(StorageFactory::slots),
            Codec.BOOL.optionalFieldOf("invert_selection", false).forGetter(StorageFactory::invertSelection)
    ).apply(instance, StorageFactory::new));

    public static final Codec<StorageFactory> CODEC = MappedAlternativeCodecs.of(FACTORY_CODEC, Codecs.NON_NEGATIVE_INT.listOf(), list -> new StorageFactory(list, false), Codec.BOOL, store -> new StorageFactory(List.of(), store));

    public UiStorage create(UiTemplate template, SimpleGui gui, NbtCompound nbtArgument, Consumer<NbtCompound> nbtWriteCallback, Consumer<NbtCompound> nbtReadCallback) {
        DataSaver dataSaver = template.save.map(
                store -> store ? DataSaver.forPlayer(gui.getPlayer(), UiRegistries.UI_REGISTRY.getIdOrNull(template), true) : null,
                saver -> saver
        );

        return new UiStorage(gui, this.invertSelection, this.slots, dataSaver, template.onClose, nbtArgument, nbtWriteCallback, nbtReadCallback);
    }
}
