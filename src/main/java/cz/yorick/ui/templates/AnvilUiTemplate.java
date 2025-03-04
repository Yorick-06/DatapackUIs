package cz.yorick.ui.templates;

import com.mojang.serialization.MapCodec;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.ui.UiTemplate;
import cz.yorick.ui.uis.AnvilUi;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class AnvilUiTemplate extends UiTemplate {
    public static final MapCodec<AnvilUiTemplate> CODEC = addCodecsAndValidator(ClassFieldsCodec.builder(UiTemplate.class, AnvilUiTemplate::new)).buildMap();

    @Override
    public SimpleGui getNewGui(ServerPlayerEntity player, NbtCompound nbtArgument) {
        return new AnvilUi(this, player, nbtArgument);
    }

    @Override
    public MapCodec<? extends UiTemplate> getCodec() {
        return CODEC;
    }

    @Override
    public ScreenHandlerType<?> getHandlerType() {
        return ScreenHandlerType.ANVIL;
    }
}
