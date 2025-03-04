package cz.yorick.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.api.codec.CodecUtils;
import cz.yorick.api.codec.MappedAlternativeCodecs;
import cz.yorick.element.elements.RefreshableElement;
import cz.yorick.util.Executable;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record ClickHandler(Executable defaultExecutable, Map<ClickType, Executable> executables, boolean refreshOnClick) implements GuiElementInterface.ClickCallback {
    private static final Set<String> CLICK_TYPE_KEYS = Arrays.stream(ClickType.values()).map(value -> value.name().toLowerCase()).collect(Collectors.toSet());
    private static final Codec<ClickHandler> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Executable.CODEC.optionalFieldOf("default").forGetter(handlder -> Optional.ofNullable(handlder.defaultExecutable())),
            CodecUtils.unboundedMap(CLICK_TYPE_KEYS, CodecUtils.caseConvertingEnum(ClickType.class), Executable.CODEC).forGetter(ClickHandler::executables),
            Codec.BOOL.optionalFieldOf("refresh", false).forGetter(ClickHandler::refreshOnClick)
    ).apply(instance, (defaultExecutable, executables, refreshOnClick) -> new ClickHandler(defaultExecutable.orElse(null), executables, refreshOnClick)));

    public static final Codec<ClickHandler> CODEC = MappedAlternativeCodecs.of(FULL_CODEC, Executable.CODEC, executable -> new ClickHandler(executable, Map.of(), false));

    @Override
    public void click(int index, ClickType type, SlotActionType action, SlotGuiInterface gui) {
        Executable executable = this.executables.getOrDefault(type, this.defaultExecutable);
        if(executable != null) {
            executable.execute(gui.getPlayer().getCommandSource(), getReturnValueConsumer(gui, index));
            return;
        }

        //if no executable is present but should still refresh
        if(this.refreshOnClick && gui.getSlot(index) instanceof RefreshableElement refreshableElement) {
            refreshableElement.refresh(gui);
        }
    }

    private ReturnValueConsumer getReturnValueConsumer(SlotGuiInterface gui, int index) {
        if(!this.refreshOnClick) {
            return ReturnValueConsumer.EMPTY;
        }

        return (successful, returnValue) -> {
            if(gui.getSlot(index) instanceof RefreshableElement refreshableElement) {
                refreshableElement.refresh(gui);
            }
        };
    }
}
