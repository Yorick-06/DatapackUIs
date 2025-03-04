package cz.yorick.placeholders;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import org.jetbrains.annotations.Nullable;

public interface ValidatedPlaceholderHandler extends PlaceholderHandler {
    @Override
    default PlaceholderResult onPlaceholderRequest(PlaceholderContext context, @Nullable String argument) {
        if(argument == null) {
            return PlaceholderResult.invalid("Argument missing");
        }

        if(!context.hasEntity()) {
            return PlaceholderResult.invalid("Entity missing");
        }

        return onValidatedRequest(context, argument);
    }

    PlaceholderResult onValidatedRequest(PlaceholderContext context, String argument);
}
