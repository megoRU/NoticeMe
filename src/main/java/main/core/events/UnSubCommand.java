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
        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();
        User userFromOptions = event.getOption("user", OptionMapping::getAsUser);
        if (userFromOptions == null) return;

        unsub(user.getId(), guildIdString, userFromOptions.getId(), event);
    }

    public void unsub_v2(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        Long userFromOptions = event.getOption("user_id", OptionMapping::getAsLong);
        if (userFromOptions == null) return;

        unsub(user.getId(), guildIdString, userFromOptions.toString(), event);
    }

    private void unsub(String userId, String guildId, String userFromOptions, @NotNull SlashCommandInteractionEvent event) {
        long userIdLong = Long.parseLong(userId);
        long guildIdLong = Long.parseLong(guildId);

        Subs notice = noticeRepository.findTrackingUser(userIdLong, guildIdLong, userFromOptions);

        if (notice == null) {
            String dontFindUser = jsonParsers.getTranslation("dont_find_user", guildId);
            event.getHook().sendMessage(dontFindUser).queue();
        } else {
            noticeRepository.deleteByUserTrackingId(notice.getUserTrackingId(), userIdLong);

            NoticeRegistry instance = NoticeRegistry.getInstance();
            instance.unsub(guildId, userFromOptions, userId);

            String successfullyDeleted = String.format(jsonParsers.getTranslation("successfully_deleted", guildId), notice.getUserTrackingId());
            event.getHook().sendMessage(successfullyDeleted).setEphemeral(true).queue();
        }
    }
}