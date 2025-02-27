package main.core.events;

import lombok.AllArgsConstructor;
import main.core.NoticeMeUtils;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class SuggestionCommand {

    private static final ParserClass jsonParsers = new ParserClass();
    private final static NoticeRegistry instance = NoticeRegistry.getInstance();

    public void suggestion(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var user = event.getUser();

        Set<String> suggestions = instance.getSuggestionsList(user.getId(), guildIdString);

        List<String> top5Users = suggestions.stream()
                .limit(5)
                .toList();

        if (!top5Users.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Button> buttonsList = new ArrayList<>();

            for (int i = 0; i < top5Users.size(); i++) {
                if (stringBuilder.isEmpty()) {
                    stringBuilder.append((i + 1)).append(". ").append("<@").append(top5Users.get(i)).append(">");
                } else {
                    stringBuilder.append("\n").append((i + 1)).append(". ").append("<@").append(top5Users.get(i)).append(">");
                }
                User userFromBD = event.getJDA().retrieveUserById(top5Users.get(i)).complete();
                String addUser = String.format(jsonParsers.getTranslation("add_user", guildIdString), userFromBD.getGlobalName());

                buttonsList.add(Button.primary(NoticeMeUtils.BUTTON_ADD_USER + top5Users.get(i), addUser));
            }

            String suggestionText = String.format(jsonParsers.getTranslation("suggestion_text", guildIdString), stringBuilder);

            event.reply(suggestionText).setActionRow(buttonsList).setEphemeral(true).queue();
        } else {
            String noSuggestions = jsonParsers.getTranslation("no_suggestions", guildIdString);
            event.reply(noSuggestions).setEphemeral(true).queue();
        }
    }
}