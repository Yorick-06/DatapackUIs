package cz.yorick.placeholders;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;

public class TagPlaceholder implements ValidatedPlaceholderHandler {
    private final boolean shouldHaveTag;
    public TagPlaceholder(boolean shouldHaveTag) {
        this.shouldHaveTag = shouldHaveTag;
    }

    @Override
    public PlaceholderResult onValidatedRequest(PlaceholderContext context, String argument) {
        int space = argument.indexOf(' ');
        if(space == -1) {
            return PlaceholderResult.invalid("Space separating the tag and text missing!");
        }

        boolean hasTag = context.entity().getCommandTags().contains(argument.substring(0, space));
        if((this.shouldHaveTag && hasTag) || (!this.shouldHaveTag && !hasTag)) {
            return PlaceholderResult.value(argument.substring(space + 1));
        }

        return PlaceholderResult.value("");
    }
}
