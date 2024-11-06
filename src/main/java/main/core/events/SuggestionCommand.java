package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.NoticeMeUtils;
import main.jsonparser.ParserClass;
import main.model.entity.Subs;
import main.model.entity.Suggestions;
import main.model.repository.NoticeRepository;
import main.model.repository.SuggestionsRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SuggestionCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final NoticeRepository noticeRepository;
    private final SuggestionsRepository suggestionsRepository;

    public void suggestion(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildId = event.getGuild().getIdLong();
        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        List<Suggestions> suggestionsList = suggestionsRepository.findAllByUserId(user.getIdLong());
        List<Subs> allSubs = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildId);

        List<String> stringList = suggestionsList
                .stream()
                .map(Suggestions::getSuggestionUserId)
                .map(String::valueOf)
                .distinct()
                .filter(a -> !BotStartConfig.getMapLocks().containsKey(a))
                .filter(a -> allSubs.stream()
                        .map(Subs::getUserTrackingId)
                        .noneMatch(s -> s.contains(a)))
                .toList();

        Map<String, Long> frequencyMap = stringList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<String> top5Users = frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
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

            event.getHook().sendMessage(suggestionText)
                    .setActionRow(buttonsList)
                    .setEphemeral(true)
                    .queue();
        } else {
            String noSuggestions = jsonParsers.getTranslation("no_suggestions", guildIdString);
            event.getHook().sendMessage(noSuggestions).setEphemeral(true).queue();
        }
    }
}