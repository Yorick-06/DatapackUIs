package cz.yorick;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import cz.yorick.api.registry.SimpleDynamicRegistry;
import cz.yorick.api.registry.SimpleRegistry;
import cz.yorick.element.ElementTemplate;
import cz.yorick.element.templates.DynamicElementTemplate;
import cz.yorick.element.templates.SimpleElementTemplate;
import cz.yorick.element.templates.ToggleableElementTemplate;
import cz.yorick.ui.UiTemplate;
import cz.yorick.ui.templates.AnvilUiTemplate;
import cz.yorick.ui.templates.ChestContainerUiTemplate;
import cz.yorick.ui.templates.MerchantUiTemplate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class UiRegistries {
    private static final SimpleRegistry<String, MapCodec<? extends ElementTemplate>> ELEMENT_TYPE_REGISTRY = new SimpleRegistry<>(Identifier.of(DatapackUIs.MOD_ID, "element_type"), Codecs.NON_EMPTY_STRING);
    public static final Codec<ElementTemplate> ELEMENT_TEMPLATE_CODEC = ELEMENT_TYPE_REGISTRY.getCodec().dispatch("type", ElementTemplate::getCodec, codec -> codec);
    public static final SimpleDynamicRegistry<Identifier, ElementTemplate> ELEMENT_REGISTRY = SimpleDynamicRegistry.ofDatapackResource(Identifier.of(DatapackUIs.MOD_ID, "ui_element"), ELEMENT_TEMPLATE_CODEC);

    private static final SimpleRegistry<String, MapCodec<? extends UiTemplate>> UI_TYPE_REGISTRY = new SimpleRegistry<>(Identifier.of(DatapackUIs.MOD_ID, "ui_type"), Codecs.NON_EMPTY_STRING);
    private static final Codec<UiTemplate> UI_TEMPLATE_CODEC = UI_TYPE_REGISTRY.getCodec().dispatch("type", UiTemplate::getCodec, codec -> codec);
    public static final SimpleRegistry<Identifier, UiTemplate> UI_REGISTRY = SimpleDynamicRegistry.ofDatapackResource(Identifier.of(DatapackUIs.MOD_ID, "ui"), UI_TEMPLATE_CODEC);
    public static void init() {
        registerUi("chest", ChestContainerUiTemplate.CODEC);
        registerUi("3x3", UiTemplate.basicCodec(ScreenHandlerType.GENERIC_3X3));
        //register("crafter", UiTemplate.basicCodec(ScreenHandlerType.CRAFTER_3X3)); //crashes the game
        registerUi("anvil", AnvilUiTemplate.CODEC);
        registerUi("beacon", UiTemplate.basicCodec(ScreenHandlerType.BEACON));
        //register("blast_furnace", UiTemplate.basicCodec(ScreenHandlerType.BLAST_FURNACE)); //same as furnace
        registerUi("brewing_stand", UiTemplate.basicCodec(ScreenHandlerType.BREWING_STAND));
        registerUi("crafting", UiTemplate.basicCodec(ScreenHandlerType.CRAFTING));
        registerUi("enchanting_table", UiTemplate.basicCodec(ScreenHandlerType.ENCHANTMENT));
        registerUi("furnace", UiTemplate.basicCodec(ScreenHandlerType.FURNACE));
        registerUi("grindstone", UiTemplate.basicCodec(ScreenHandlerType.GRINDSTONE));
        registerUi("hopper", UiTemplate.basicCodec(ScreenHandlerType.HOPPER));
        //register("lectern", UiTemplate.basicCodec(ScreenHandlerType.LECTERN)); //crashes the game
        registerUi("loom", UiTemplate.basicCodec(ScreenHandlerType.LOOM));
        registerUi("merchant", MerchantUiTemplate.CODEC);
        //register("shulker_box", UiTemplate.basicCodec(ScreenHandlerType.SHULKER_BOX)); //same as 3x9 basic
        registerUi("smithing", UiTemplate.basicCodec(ScreenHandlerType.SMITHING));
        //register("smoker", UiTemplate.basicCodec(ScreenHandlerType.SMOKER)); //same as furnace
        registerUi("cartography", UiTemplate.basicCodec(ScreenHandlerType.CARTOGRAPHY_TABLE));
        registerUi("stonecutter", UiTemplate.basicCodec(ScreenHandlerType.STONECUTTER));

        registerElement("simple", SimpleElementTemplate.CODEC);
        registerElement("toggle", ToggleableElementTemplate.CODEC);
        registerElement("dynamic", DynamicElementTemplate.CODEC);
    }

    private static void registerUi(String id, MapCodec<? extends UiTemplate> codec) {
        UI_TYPE_REGISTRY.register(id, codec);
    }

    private static void registerElement(String id, MapCodec<? extends ElementTemplate> codec) {
        ELEMENT_TYPE_REGISTRY.register(id, codec);
    }
}
