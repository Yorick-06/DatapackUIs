package cz.yorick.element.templates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import cz.yorick.UiRegistries;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.api.codec.annotations.FieldId;
import cz.yorick.api.codec.annotations.IncludeParent;
import cz.yorick.element.ElementTemplate;
import cz.yorick.element.elements.DynamicUiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

@IncludeParent
public class DynamicElementTemplate extends ElementTemplate {
    public static MapCodec<DynamicElementTemplate> CODEC = addCodecs(ClassFieldsCodec.builder(DynamicElementTemplate.class)).withCodec(Codec.unboundedMap(Predicate.CODEC, UiRegistries.ELEMENT_TEMPLATE_CODEC), "alternative_elements").buildMap();

    @FieldId(id = "alternative_elements")
    private final Map<Predicate, ElementTemplate> alternativeElements = Map.of();

    @Override
    public GuiElementInterface createNew(ServerPlayerEntity player) {
        return new DynamicUiElement(this, this.createDefault());
    }

    public GuiElementInterface createDefault() {
        return this.display.create(this.name, this.lore, this.onClick);
    }

    //get template specific for that player
    public ElementTemplate getTemplate(ServerPlayerEntity player) {
        LootContext context = getLootContext(player);
        for (Map.Entry<Predicate, ElementTemplate> entry : this.alternativeElements.entrySet()) {
            if(entry.getKey().test(player, context)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static LootContext getLootContext(ServerPlayerEntity player) {
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(player.getServerWorld()).add(LootContextParameters.ORIGIN, player.getPos()).add(LootContextParameters.THIS_ENTITY, player).build(LootContextTypes.COMMAND);
        LootContext lootContext = new LootContext.Builder(lootWorldContext).build(Optional.empty());
        return lootContext;
    }

    @Override
    public MapCodec<? extends ElementTemplate> getCodec() {
        return CODEC;
    }

    private record Predicate(String input, BiPredicate<ServerPlayerEntity, LootContext> tester) {
         private static final Codec<Predicate> CODEC = Codec.either(LootCondition.ENTRY_CODEC, Codecs.NON_EMPTY_STRING).xmap(
         either -> either.map(
                lootCondition -> new Predicate(lootCondition.getIdAsString(), (player, lootContext) -> {
                    //lootContext.markActive(LootContext.predicate(lootCondition.value()));
                    System.out.println("Created predicate " + lootCondition.getIdAsString());
                    return lootCondition.value().test(lootContext);
                }),
                string -> new Predicate(string, (player, lootContext) -> player.getCommandTags().contains(string))
         ), predicate -> Either.right(predicate.input()));

        //private static final Codec<Predicate> CODEC = LootCondition.ENTRY_CODEC.xmap(condition -> new Predicate(condition.getIdAsString(), (player, context) -> condition.value().test(context)), value -> null);

    //private static final Codec<Predicate> CODEC = RegistryFixedCodec.of(RegistryKeys.PREDICATE).xmap(condition -> new Predicate(condition.getIdAsString(), (player, context) -> condition.value().test(context)), value -> null);

         private boolean test(ServerPlayerEntity player, LootContext context) {
             return this.tester.test(player, context);
         }
    }
}
