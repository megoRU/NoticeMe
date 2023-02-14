package main.core.events;

import main.config.BotStartConfig;
import main.core.ButtonImpl;
import main.jsonparser.ParserClass;
import main.model.entity.Entries;
import main.model.entity.Subs;
import main.model.repository.EntriesRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class SuggestionCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final EntriesRepository entriesRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    public SuggestionCommand(EntriesRepository entriesRepository, NoticeRepository noticeRepository) {
        this.entriesRepository = entriesRepository;
        this.noticeRepository = noticeRepository;
    }

    public void suggestion(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildId = event.getGuild().getIdLong();
        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        List<Entries> allEntriesForSuggestion = entriesRepository.getAllEntriesForSuggestion(user.getIdLong(), guildId);
        List<Subs> allSubs = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildId);

        List<String> stringList = allEntriesForSuggestion
                .stream()
                .map(Entries::getUsersInChannel)
                .flatMap(string -> Stream.of(string.split(",")))
                .distinct()
                .filter(a -> !BotStartConfig.mapLocks.containsKey(a))
                .filter(a -> allSubs.stream()
                        .map(Subs::getUserTrackingId)
                        .noneMatch(s -> s.contains(a)))
                .toList();

        if (!stringList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Button> buttonsList = new ArrayList<>();

            for (int i = 0; i < stringList.size(); i++) {
                if (stringBuilder.isEmpty()) {
                    stringBuilder.append((i + 1)).append(". ").append("<@").append(stringList.get(i)).append(">");
                } else {
                    stringBuilder.append("\n").append((i + 1)).append(". ").append("<@").append(stringList.get(i)).append(">");
                }
                if (buttonsList.size() <= 23) {
                    String addUser = String.format(jsonParsers.getTranslation("add_user", guildIdString), (i + 1));
                    buttonsList.add(Button.primary(ButtonImpl.BUTTON_ADD_USER + (i + 1), addUser));
                }
            }

            if (buttonsList.size() > 1) {
                String addAll = jsonParsers.getTranslation("add_all", guildIdString);
                buttonsList.add(Button.success(ButtonImpl.BUTTON_ALL_USERS, addAll));
            }

            String suggestionText = String.format(jsonParsers.getTranslation("suggestion_text", guildIdString), stringBuilder);
            WebhookMessageCreateAction<Message> messageWebhookMessageCreateAction = event.getHook().sendMessage(suggestionText).setEphemeral(true);

            int second = Math.min(buttonsList.size(), 4);
            int first = 0;
            int ceil = (int) Math.ceil(buttonsList.size() / 5.0);
            for (int i = 0; i < ceil; i++) {
                messageWebhookMessageCreateAction.addActionRow(buttonsList.subList(first, second));
                first = second;
                second += 4;
            }
            messageWebhookMessageCreateAction.queue();
        } else {
            String noSuggestions = jsonParsers.getTranslation("no_suggestions", guildIdString);
            event.getHook().sendMessage(noSuggestions).setEphemeral(true).queue();
        }
    }
}