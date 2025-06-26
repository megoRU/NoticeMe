package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.core.core.Suggestions;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.Guild;
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
        Guild guild = Objects.requireNonNull(event.getGuild());

        var user = event.getUser();
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        event.editButton(event.getButton().asDisabled()).queue();

        Long userFromButton = Long.valueOf(buttonId.replaceAll("BUTTON_ADD_USER_", ""));

        if (BotStartConfig.getMapLocks().containsKey(userFromButton.toString())) {
            String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guild.getId());
            event.getHook().sendMessage(cannotSubToThisUser).setEphemeral(true).queue();
            return;
        }

        TrackingUser trackingUser = instance.getUser(guild.getIdLong(), user.getIdLong());

        if (trackingUser != null) {
            Set<Long> userListSet = trackingUser.getUserListSet();
            if (userListSet.contains(userFromButton)) {
                String youAlreadyTracked = jsonParsers.getTranslation("you_already_tracked", guild.getId());
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
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();

        var user = event.getUser();
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;
        Long userFromButton = Long.valueOf(buttonId.replaceAll("BUTTON_ADD_USER_", ""));

        Server server = instance.getServer(guildIdLong);
        Long setup = BotStartConfig.getCommandId("setup");

        if (server == null) {
            String youCannotSetChannel = String.format(jsonParsers.getTranslation("you_cannot_set_channel", guildIdString), setup);
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
            return;
        }

        Subs subs = new Subs();
        subs.setServer(server);
        subs.setUserId(user.getIdLong());
        subs.setUserTrackingId(userFromButton.toString());
        noticeRepository.save(subs);

        String userSaved = String.format(jsonParsers.getTranslation("user_saved", guildIdString), userFromButton);
        event.getHook().sendMessage(userSaved).setEphemeral(true).queue();

        instance.sub(guildIdLong, user.getIdLong(), userFromButton);

        Suggestions suggestions = instance.getSuggestions(guildIdLong, user.getIdLong());
        if (suggestions != null) suggestions.removeUser(userFromButton);
    }
}