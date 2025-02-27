package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class AddUserButton {

    private static final ParserClass jsonParsers = new ParserClass();
    private static final NoticeRegistry instance = NoticeRegistry.getInstance();
    private final NoticeRepository noticeRepository;

    public void addUser(@NotNull ButtonInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var user = event.getUser();
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        event.editButton(event.getButton().asDisabled()).queue();

        String userFromButton = buttonId.replaceAll("BUTTON_ADD_USER_", "");

        if (BotStartConfig.getMapLocks().containsKey(userFromButton)) {
            String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guildIdString);
            event.getHook().sendMessage(cannotSubToThisUser).setEphemeral(true).queue();
            return;
        }

        TrackingUser trackingUser = instance.getUser(guildIdString, user.getId());

        if (trackingUser != null) {
            Set<String> userListSet = trackingUser.getUserListSet();
            if (userListSet.contains(userFromButton)) {
                String youAlreadyTracked = jsonParsers.getTranslation("you_already_tracked", guildIdString);
                event.getHook().sendMessage(youAlreadyTracked).setEphemeral(true).queue();
            } else {
                saveUserTracking(event);
            }
        } else {
            saveUserTracking(event);
        }
    }

    private void saveUserTracking(@NotNull ButtonInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var user = event.getUser();
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;
        String userFromButton = buttonId.replaceAll("BUTTON_ADD_USER_", "");

        Server server = instance.getServer(guildIdString);

        if (server == null) {
            String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
            return;
        }

        Subs subs = new Subs();
        subs.setServer(server);
        subs.setUserId(user.getIdLong());
        subs.setUserTrackingId(userFromButton);
        noticeRepository.save(subs);

        String userSaved = String.format(jsonParsers.getTranslation("user_saved", guildIdString), userFromButton);
        event.getHook().sendMessage(userSaved).setEphemeral(true).queue();

        instance.sub(guildIdString, user.getId(), userFromButton);
    }
}