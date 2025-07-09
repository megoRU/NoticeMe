package main.core.events;

import lombok.AllArgsConstructor;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class ListCommand {

    private static final ParserClass jsonParsers = new ParserClass();
    private static final NoticeRegistry instance = NoticeRegistry.getInstance();

    public void list(@NotNull SlashCommandInteractionEvent event) {
        var user = event.getUser();
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();

        Set<Long> allUserTrackerIdsByUserId = instance.getUserTrackerIdsByUserId(guildIdLong, user.getIdLong());

        if (!allUserTrackerIdsByUserId.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Long userNotice : allUserTrackerIdsByUserId) {
                if (stringBuilder.isEmpty()) {
                    stringBuilder.append("<@").append(userNotice).append(">");
                } else {
                    stringBuilder.append(", <@").append(userNotice).append(">");
                }
            }
            String subscription = String.format(jsonParsers.getTranslation("subscription", guildIdString), stringBuilder);
            event.reply(subscription).setEphemeral(true).queue();
        } else {
            String emptyList = jsonParsers.getTranslation("empty_list", guildIdString);
            event.reply(emptyList).setEphemeral(true).queue();
        }
    }
}