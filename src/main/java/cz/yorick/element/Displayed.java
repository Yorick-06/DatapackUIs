package cz.yorick.element;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.api.codec.MappedAlternativeCodecs;
import cz.yorick.element.elements.AnimatedUiElement;
import cz.yorick.element.elements.StaticUiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Optional;

public interface Displayed {
    Codec<ItemStack> STACK_CODEC = MappedAlternativeCodecs.of(ItemStack.VALIDATED_CODEC, Registries.ITEM.getCodec(), ItemStack::new, Codec.string(180, 180), Displayed::playerHead);
    private static ItemStack playerHead(String texture) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        Property property = new Property("textures", texture);
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(property.name(), property);
        ProfileComponent profileComponent = new ProfileComponent(Optional.empty(), Optional.empty(), propertyMap);
        stack.set(DataComponentTypes.PROFILE, profileComponent);
        return stack;
    }

    Codec<Displayed> CODEC = Codec.either(Static.CODEC, Animated.CODEC).xmap(Displayed::of, Displayed::serialize);
    GuiElementInterface create(PersonalizedText name, List<PersonalizedText> lore, GuiElementInterface.ClickCallback callback);
    Either<Static, Animated> serialize();

    static Displayed of(Either<Static, Animated> either) {
        return either.map(
                staticDisplay -> staticDisplay,
                animatedDisplay -> animatedDisplay
        );
    }

    record Static(ItemStack stack) implements Displayed {
        private static final Codec<Static> CODEC = STACK_CODEC.xmap(Static::new, Static::stack);

        @Override
        public GuiElementInterface create(PersonalizedText name, List<PersonalizedText> lore, GuiElementInterface.ClickCallback callback) {
            return new StaticUiElement(this.stack, name, lore, callback);
        }

        @Override
        public Either<Static, Animated> serialize() {
            return Either.left(this);
        }
    }

    record Animated(List<ItemStack> stacks, int interval, boolean random) implements Displayed {
        private static final Codec<Animated> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                STACK_CODEC.listOf().fieldOf("stacks").forGetter(Animated::stacks),
                Codecs.POSITIVE_INT.fieldOf("interval").forGetter(Animated::interval),
                Codec.BOOL.optionalFieldOf("random", false).forGetter(Animated::random)
        ).apply(instance, Animated::new));

        @Override
        public GuiElementInterface create(PersonalizedText name, List<PersonalizedText> lore, GuiElementInterface.ClickCallback callback) {
            return new AnimatedUiElement(this.stacks, this.interval, this.random, name, lore, callback);
        }

        @Override
        public Either<Static, Animated> serialize() {
            return Either.right(this);
        }
    }
}
