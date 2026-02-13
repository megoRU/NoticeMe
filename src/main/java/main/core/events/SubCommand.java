package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class SubCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final NoticeRepository noticeRepository;
    private final static NoticeRegistry instance = NoticeRegistry.getInstance();

    public void sub(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = event.getGuild().getIdLong();
        var user = event.getUser();

        event.deferReply().setEphemeral(true).queue();

        User userDest = event.getOption("user", OptionMapping::getAsUser);

        if (userDest == null) {
            event.getHook().sendMessage("user is null").setEphemeral(true).queue();
            return;
        } else if (userDest.getIdLong() == user.getIdLong()) {
            String yourself = jsonParsers.getTranslation("yourself", guildIdString);
            event.getHook().sendMessage(yourself).setEphemeral(true).queue();
            return;
        } else if (userDest.isBot()) {
            String bot = jsonParsers.getTranslation("bot", guildIdString);
            event.getHook().sendMessage(bot).setEphemeral(true).queue();
            return;
        }

        if (BotStartConfig.getMapLocks().containsKey(userDest.getId())) {
            String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guildIdString);
            event.getHook().sendMessage(cannotSubToThisUser).setEphemeral(true).queue();
            return;
        }

        Server server = instance.getServer(guildIdLong);

        if (server != null) {
            TrackingUser trackingUser = instance.getUser(guildIdLong, userDest.getIdLong());

            if (trackingUser != null) {
                Set<Long> userListSet = trackingUser.getUserListSet();
                if (userListSet.contains(user.getIdLong())) {
                    String youAlreadyTracked = jsonParsers.getTranslation("you_already_tracked", guildIdString);
                    event.getHook().sendMessage(youAlreadyTracked).setEphemeral(true).queue();
                } else {
                    saveUserTracking(event, server, userDest);
                }
            } else {
                saveUserTracking(event, server, userDest);
            }
        } else {
            String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
        }
    }

    private void saveUserTracking(@NotNull SlashCommandInteractionEvent event, Server server, @NotNull User userDest) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        Long guildIdLong = event.getGuild().getIdLong();
        var user = event.getUser();

        Subs notice = new Subs();
        notice.setServer(server);
        notice.setUserId(user.getIdLong());
        notice.setUserTrackingId(userDest.getId());
        noticeRepository.save(notice);

        instance.sub(guildIdLong, user.getIdLong(), userDest.getIdLong());

        String nowYouWillReceive = String.format(jsonParsers.getTranslation("now_you_will_receive", guildIdString), userDest.getIdLong());
        event.getHook().sendMessage(nowYouWillReceive).setEphemeral(true).queue();
    }
}