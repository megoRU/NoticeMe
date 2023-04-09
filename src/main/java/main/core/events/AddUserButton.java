package main.core.events;

import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.GuildRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AddUserButton {
    private static final ParserClass jsonParsers = new ParserClass();

    private final GuildRepository guildRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    public AddUserButton(GuildRepository guildRepository, NoticeRepository noticeRepository) {
        this.guildRepository = guildRepository;
        this.noticeRepository = noticeRepository;
    }

    public void addUser(@NotNull ButtonInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = event.getGuild().getIdLong();
        var user = event.getUser();
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        System.out.println("buttonId " + buttonId);

        event.editButton(event.getButton().asDisabled()).queue();

        String userFromButton = buttonId.replaceAll("BUTTON_ADD_USER_", "");

        if (BotStartConfig.mapLocks.containsKey(userFromButton)) {
            String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guildIdString);
            event.getHook().sendMessage(cannotSubToThisUser).setEphemeral(true).queue();
            return;
        }

        //TODO: Возможно быстрее проверять локально
        Subs isUserContains = noticeRepository.findAllByUserIdAndUserTrackingId(guildIdLong, userFromButton, user.getIdLong());

        if (isUserContains == null) {
            Server serverId = guildRepository.getReferenceById(event.getGuild().getIdLong());
            Subs subs = new Subs();
            subs.setServer(serverId);
            subs.setUserId(event.getUser().getIdLong());
            subs.setUserTrackingId(userFromButton);
            noticeRepository.save(subs);
            String userSaved = jsonParsers.getTranslation("user_saved", guildIdString);
            event.getHook().sendMessage(userSaved).setEphemeral(true).queue();

            NoticeRegistry instance = NoticeRegistry.getInstance();
            instance.sub(guildIdString, user.getId(), userFromButton);
        } else {
            String youAlreadyTracked = jsonParsers.getTranslation("you_already_tracked", guildIdString);
            event.getHook().sendMessage(youAlreadyTracked).setEphemeral(true).queue();
        }
    }
}