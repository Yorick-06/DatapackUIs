package cz.yorick.element;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.TagParser;
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;

import java.util.List;

//parses the string with quick text or accepts text in json format
public record PersonalizedText(String input, Text text) {
    private static final TagParser PARSER = TagParser.createQuickTextWithSTF(TagRegistry.DEFAULT);
    public static final Codec<PersonalizedText> CODEC = Codec.either(Codec.STRING, TextCodecs.CODEC).xmap(PersonalizedText::of, PersonalizedText::serialize);
    private static final Style NON_ITALIC = Style.EMPTY.withItalic(false);

    public static PersonalizedText of(Either<String, Text> either) {
        return either.map(
                string -> new PersonalizedText(string, PARSER.parseText(string, ParserContext.of())),
                text -> new PersonalizedText(null, text)
        );
    }

    public Either<String, Text> serialize() {
        return this.input != null ? Either.left(this.input) : Either.right(this.text);
    }

    public Text getFor(ServerPlayerEntity player) {
        return Placeholders.parseText(this.text, PlaceholderContext.of(player));
    }

    public static ItemStack personalizeStack(ServerPlayerEntity player, ItemStack original, PersonalizedText name, List<PersonalizedText> lore) {
        ItemStack stack = original.copy();
        if(name != null) {
            stack.set(DataComponentTypes.CUSTOM_NAME, Texts.setStyleIfAbsent(name.getFor(player).copy(), NON_ITALIC));
        }

        if(lore.size() > 0) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore.stream()
                    .map(element -> (Text)Texts.setStyleIfAbsent(element.getFor(player).copy(), NON_ITALIC)).toList()
            ));
        }

        return stack;
    }
}
