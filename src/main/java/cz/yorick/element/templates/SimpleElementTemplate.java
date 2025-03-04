package cz.yorick.element.templates;

import com.mojang.serialization.MapCodec;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.element.ElementTemplate;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.server.network.ServerPlayerEntity;

public class SimpleElementTemplate extends ElementTemplate {
    public static final MapCodec<SimpleElementTemplate> CODEC = addCodecs(ClassFieldsCodec.builder(ElementTemplate.class, SimpleElementTemplate::new)).buildMap();

    @Override
    public GuiElementInterface createNew(ServerPlayerEntity player) {
        return this.display.create(this.name, this.lore, this.onClick);
    }

    @Override
    public MapCodec<? extends ElementTemplate> getCodec() {
        return CODEC;
    }
}
