package main.core.events;

import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.GuildRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class SubCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final GuildRepository guildRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    public SubCommand(GuildRepository guildRepository, NoticeRepository noticeRepository) {
        this.guildRepository = guildRepository;
        this.noticeRepository = noticeRepository;
    }

    public void sub(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildId = event.getGuild().getIdLong();
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

        if (BotStartConfig.getMapLanguages().containsKey(userDest.getId())) {
            String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guildIdString);
            event.getHook().sendMessage(cannotSubToThisUser).setEphemeral(true).queue();
            return;
        }

        Optional<Server> guildOptional = guildRepository.findById(guildId);
        if (guildOptional.isPresent()) {
            Subs trackingUser = noticeRepository.findTrackingUser(user.getIdLong(), guildId, userDest.getId());

            if (trackingUser == null) {
                Subs notice = new Subs();
                notice.setServer(guildOptional.get());
                notice.setUserId(user.getIdLong());
                notice.setUserTrackingId(userDest.getId());
                noticeRepository.save(notice);

                NoticeRegistry instance = NoticeRegistry.getInstance();
                instance.sub(guildIdString, user.getId(), userDest.getId());

                String nowYouWillReceive = String.format(jsonParsers.getTranslation("now_you_will_receive", guildIdString), userDest.getIdLong());
                event.getHook().sendMessage(nowYouWillReceive).setEphemeral(true).queue();
            } else {
                String youAlreadyTracked = jsonParsers.getTranslation("you_already_tracked", guildIdString);
                event.getHook().sendMessage(youAlreadyTracked).setEphemeral(true).queue();
            }
        } else {
            String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
        }
    }
}