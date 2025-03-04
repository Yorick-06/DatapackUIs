package cz.yorick.placeholders;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AttributePlaceholder implements ValidatedPlaceholderHandler {
    @Override
    public PlaceholderResult onValidatedRequest(PlaceholderContext context, String argument) {
        Identifier attributeId = Identifier.tryParse(argument);
        if(attributeId == null) {
            return PlaceholderResult.invalid("Invalid identifier '" + argument + "'");
        }

        EntityAttribute attribute = Registries.ATTRIBUTE.get(attributeId);
        if(attribute == null) {
            return PlaceholderResult.invalid("Invalid attribute id '" + argument + "'");
        }

        if(context.entity() instanceof LivingEntity livingEntity) {
            return PlaceholderResult.value(Text.literal(String.valueOf(livingEntity.getAttributeValue(Registries.ATTRIBUTE.getEntry(attribute)))));
        }
        return PlaceholderResult.invalid("Provided entity is not a LivingEntity");
    }
}
