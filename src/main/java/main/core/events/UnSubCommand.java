package main.core.events;

import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Subs;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UnSubCommand {
    private static final ParserClass jsonParsers = new ParserClass();

    private final NoticeRepository noticeRepository;

    public UnSubCommand(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public void unsub(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildId = event.getGuild().getIdLong();
        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();
        User userFromOptions = event.getOption("user", OptionMapping::getAsUser);
        if (userFromOptions == null) return;
        Subs notice = noticeRepository.findTrackingUser(user.getIdLong(), guildId, userFromOptions.getId());

        if (notice == null) {
            String dontFindUser = jsonParsers.getTranslation("dont_find_user", guildIdString);
            event.getHook().sendMessage(dontFindUser).queue();
        } else {
            noticeRepository.deleteByUserTrackingId(notice.getUserTrackingId(), user.getIdLong());

            NoticeRegistry instance = NoticeRegistry.getInstance();
            instance.unsub(guildIdString, userFromOptions.getId(), user.getId());

            String successfullyDeleted = String.format(jsonParsers.getTranslation("successfully_deleted", guildIdString), notice.getUserTrackingId());
            event.getHook().sendMessage(successfullyDeleted).setEphemeral(true).queue();
        }
    }
}