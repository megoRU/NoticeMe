package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.core.core.Suggestions;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class AddAllUsersButton {

    private static final ParserClass jsonParsers = new ParserClass();

    private final NoticeRepository noticeRepository;
    private final static NoticeRegistry instance = NoticeRegistry.getInstance();

    public void addAllUsers(@NotNull ButtonInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();

        var user = event.getUser();
        String buttonId = event.getButton().getCustomId();
        if (buttonId == null) return;

        event.editButton(event.getButton().asDisabled()).queue();
        List<User> members = event.getMessage().getMentions().getUsers();

        Set<Long> allUserTrackerIdsByUserId = instance.getUserTrackerIdsByUserId(guildIdLong, user.getIdLong());

        // Фильтрация и сборка упомянутых пользователей
        List<User> collect = members.stream()
                .filter(users -> !allUserTrackerIdsByUserId.contains(users.getIdLong()))
                .filter(users -> !BotStartConfig.getMapLocks().containsKey(users.getId()))
                .toList();

        List<Subs> subsList = new ArrayList<>();

        Server server = instance.getServer(guildIdLong);

        if (server == null) {
            String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
            return;
        }

        for (User value : collect) {
            Subs subs = new Subs();
            subs.setServer(server);
            subs.setUserId(user.getIdLong());
            subs.setUserTrackingId(value.getId());

            subsList.add(subs);

            NoticeRegistry instance = NoticeRegistry.getInstance();
            instance.sub(guildIdLong, user.getIdLong(), value.getIdLong());

            Suggestions suggestions = instance.getSuggestions(guildIdLong, user.getIdLong());
            if (suggestions != null) suggestions.removeUser(value.getIdLong());
        }
        noticeRepository.saveAll(subsList);
        String usersSaved = jsonParsers.getTranslation("users_saved", guildIdString);
        event.getHook().sendMessage(usersSaved).setEphemeral(true).queue();
    }
}