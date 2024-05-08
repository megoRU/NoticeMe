package main.core.events;

import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.GuildRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AddAllUsersButton {

    private static final ParserClass jsonParsers = new ParserClass();

    private final GuildRepository guildRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    public AddAllUsersButton(GuildRepository guildRepository, NoticeRepository noticeRepository) {
        this.guildRepository = guildRepository;
        this.noticeRepository = noticeRepository;
    }

    public void addAllUsers(@NotNull ButtonInteractionEvent event) {
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var user = event.getUser();
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        event.editButton(event.getButton().asDisabled()).queue();
        List<User> members = event.getMessage().getMentions().getUsers();
        List<Subs> allSubs = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildIdLong);

        // Получение идентификаторов всех подписок в виде Map
        Map<String, String> userSubsMap = allSubs
                .stream()
                .collect(Collectors.toMap(Subs::getUserTrackingId, Subs::getUserTrackingId));

        // Фильтрация и сборка упомянутых пользователей
        List<User> collect = members.stream()
                .parallel() // Параллельная обработка
                .filter(u -> !userSubsMap.containsKey(u.getId()) && !BotStartConfig.mapLocks.containsKey(u.getId()))
                .toList();

        List<Subs> subsList = new ArrayList<>();
        Server server = guildRepository.findServerByGuildIdLong(guildIdLong);

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
            instance.sub(guildIdString, user.getId(), value.getId());
        }
        noticeRepository.saveAll(subsList);
        String usersSaved = jsonParsers.getTranslation("users_saved", guildIdString);
        event.getHook().sendMessage(usersSaved).setEphemeral(true).queue();
    }
}