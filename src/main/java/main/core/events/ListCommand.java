package main.core.events;

import main.jsonparser.ParserClass;
import main.model.entity.Subs;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ListCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final NoticeRepository noticeRepository;

    @Autowired
    public ListCommand(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public void list(@NotNull SlashCommandInteractionEvent event) {
        var user = event.getUser();
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildIdString = event.getGuild().getId();

        event.deferReply().setEphemeral(true).queue();

        List<Subs> noticeList = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildId);
        if (!noticeList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Subs notice : noticeList) {
                if (stringBuilder.isEmpty()) {
                    stringBuilder.append("<@").append(notice.getUserTrackingId()).append(">");
                } else {
                    stringBuilder.append(", <@").append(notice.getUserTrackingId()).append(">");
                }
            }
            String subscription = String.format(jsonParsers.getTranslation("subscription", guildIdString), stringBuilder);
            event.getHook().sendMessage(subscription).setEphemeral(true).queue();
        } else {
            String emptyList = jsonParsers.getTranslation("empty_list", guildIdString);
            event.getHook().sendMessage(emptyList).setEphemeral(true).queue();
        }
    }
}
