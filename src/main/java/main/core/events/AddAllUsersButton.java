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
import java.util.Objects;

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

        //TODO: Сделать в один filter
        List<User> collect = members
                .stream()
                .filter(a -> allSubs.stream()
                        .map(Subs::getUserTrackingId)
                        .noneMatch(s -> s.contains(a.getId())))
                .filter(m -> !BotStartConfig.mapLocks.containsKey(m.getId()))
                .toList();

        List<Subs> subsList = new ArrayList<>();
        Server serverId = guildRepository.getReferenceById(guildIdLong);

        for (User value : collect) {
            Subs subs = new Subs();
            subs.setServer(serverId);
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
