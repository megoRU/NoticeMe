package main.core.events;

import lombok.AllArgsConstructor;
import main.core.NoticeMeUtils;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger LOGGER = LoggerFactory.getLogger(SuggestionCommand.class.getName());

    public void suggestion(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        var guildIdString = Objects.requireNonNull(guild).getId();
        var user = event.getUser();

        Set<String> suggestions = instance.getSuggestionsList(guildIdString, user.getId());
        List<String> top5Users = suggestions.stream().limit(5).toList();

        StringBuilder stringBuilder = new StringBuilder();
        List<Button> buttonsList = new ArrayList<>();

        for (int i = 0; i < top5Users.size(); i++) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append((i + 1)).append(". ").append("<@").append(top5Users.get(i)).append(">");
            } else {
                stringBuilder.append("\n").append((i + 1)).append(". ").append("<@").append(top5Users.get(i)).append(">");
            }

            Member member = guild.getMemberById(top5Users.get(i));
            if (member == null) {
                try {
                    member = guild.retrieveMemberById(top5Users.get(i)).complete();
                    String addUser = String.format(jsonParsers.getTranslation("add_user", guildIdString), member.getEffectiveName());
                    buttonsList.add(Button.primary(NoticeMeUtils.BUTTON_ADD_USER + top5Users.get(i), addUser));
                } catch (ErrorResponseException e) {
                    if (e.getErrorCode() == 10007) { // UNKNOWN_MEMBER
                        LOGGER.info("UNKNOWN_MEMBER: {} Guild: {}", top5Users.get(i), guildIdString);
                    }
                }
            } else {
                String addUser = String.format(jsonParsers.getTranslation("add_user", guildIdString), member.getEffectiveName());
                buttonsList.add(Button.primary(NoticeMeUtils.BUTTON_ADD_USER + top5Users.get(i), addUser));
            }
        }

        if (buttonsList.isEmpty()) {
            String noSuggestions = jsonParsers.getTranslation("no_suggestions", guildIdString);
            event.reply(noSuggestions).setEphemeral(true).queue();
        } else {
            String suggestionText = String.format(jsonParsers.getTranslation("suggestion_text", guildIdString), stringBuilder);
            event.reply(suggestionText).setActionRow(buttonsList).setEphemeral(true).queue();
        }
    }
}