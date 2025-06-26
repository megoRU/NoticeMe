package main.core.events;

import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
public class UnSubCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final NoticeRepository noticeRepository;
    private final static NoticeRegistry instance = NoticeRegistry.getInstance();

    public UnSubCommand(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public void unsub(@NotNull SlashCommandInteractionEvent event) {
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();

        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        User userFromOptions = event.getOption("user", OptionMapping::getAsUser);
        if (userFromOptions == null) return;

        unsub(user.getIdLong(), guildIdLong, userFromOptions.getIdLong(), event);
    }

    public void unsub_v2(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();

        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        String userFromOptions = event.getOption("user-id", OptionMapping::getAsString);
        if (userFromOptions == null) return;
        if (!userFromOptions.matches("[0-9]+")) {
            String numberIdError = jsonParsers.getTranslation("number_id_error", guildIdString);
            event.getHook().sendMessage(numberIdError).queue();
            return;
        }

        unsub(user.getIdLong(), guildIdLong, Long.parseLong(userFromOptions), event);
    }

    private void unsub(Long userId, Long guildId, Long userFromOptions, @NotNull SlashCommandInteractionEvent event) {
        TrackingUser trackingUser = instance.getUser(guildId, userFromOptions);

        if (trackingUser != null) {
            Set<Long> userListSet = trackingUser.getUserListSet();
            if (userListSet.contains(userId)) {
                noticeRepository.deleteByUserTrackingId(userFromOptions.toString(), userId);
                instance.unsub(guildId, userFromOptions, userId);

                instance.addUserSuggestions(guildId, userId, userFromOptions);

                String successfullyDeleted = String.format(jsonParsers.getTranslation("successfully_deleted", guildId.toString()), userFromOptions);
                event.getHook().sendMessage(successfullyDeleted).setEphemeral(true).queue();
            } else {
                String dontFindUser = jsonParsers.getTranslation("dont_find_user", guildId.toString());
                event.getHook().sendMessage(dontFindUser).queue();
            }
        } else {
            String dontFindUser = jsonParsers.getTranslation("dont_find_user", guildId.toString());
            event.getHook().sendMessage(dontFindUser).queue();
        }
    }
}