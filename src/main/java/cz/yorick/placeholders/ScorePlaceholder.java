package cz.yorick.placeholders;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public class ScorePlaceholder implements ValidatedPlaceholderHandler {
    @Override
    public PlaceholderResult onValidatedRequest(PlaceholderContext context, String argument) {
        Scoreboard scoreboard = context.server().getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(argument);
        if(objective == null) {
            return PlaceholderResult.invalid("Invalid scoreboard '" + argument + "'");
        }

        ReadableScoreboardScore score = scoreboard.getScore(context.entity(), objective);
        if(score == null) {
            return PlaceholderResult.value(String.valueOf(0));
        }

        return PlaceholderResult.value(Text.literal(String.valueOf(score.getScore())));
    }
}
