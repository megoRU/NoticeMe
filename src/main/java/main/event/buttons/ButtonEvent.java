package main.event.buttons;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.GuildRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ButtonEvent extends ListenerAdapter {

    public static final String BUTTON_DELETE = "BUTTON_DELETE";
    public static final String BUTTON_ADD_USER = "BUTTON_ADD_USER_";
    public static final String BUTTON_ALL_USERS = "BUTTON_ALL_USERS";
    private static final ParserClass jsonParsers = new ParserClass();
    private final GuildRepository guildRepository;
    private final NoticeRepository noticeRepository;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;
        if (event.getGuild() == null) return;
        long guildIdLong = event.getGuild().getIdLong();
        String guildIdString = event.getGuild().getId();
        User user = event.getUser();

        if (Objects.equals(event.getButton().getId(), BUTTON_DELETE)) {
            event.editButton(event.getButton().asDisabled()).queue();
            guildRepository.deleteById(guildIdLong);
            NoticeRegistry.getInstance().removeGuild(guildIdString);
            String deleteData = String.format(jsonParsers.getTranslation("delete_data", guildIdString));
            event.getHook().sendMessage(deleteData).setEphemeral(true).queue();
            return;
        }

        if (event.getButton().getId().contains(BUTTON_ADD_USER)) {
            event.editButton(event.getButton().asDisabled()).queue();
            List<User> members = event.getMessage().getMentions().getUsers();
            int userNumber = Integer.parseInt(event.getButton().getId().replace("BUTTON_ADD_USER_", "")) - 1;
            User userFromMSG = members.get(userNumber);

            if (BotStartConfig.mapLocks.containsKey(userFromMSG.getId())) {
                String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guildIdString);
                event.getHook().sendMessage(cannotSubToThisUser).setEphemeral(true).queue();
                return;
            }

            Server serverId = guildRepository.getReferenceById(event.getGuild().getIdLong());
            Subs subs = new Subs();
            subs.setServer(serverId);
            subs.setUserId(event.getUser().getIdLong());
            Subs isUserContains = noticeRepository.findAllByUserIdAndUserTrackingId(userFromMSG.getId(), event.getUser().getIdLong());
            if (isUserContains == null) {
                subs.setUserTrackingId(user.getId());
                noticeRepository.save(subs);
                String userSaved = jsonParsers.getTranslation("user_saved", guildIdString);
                event.getHook().sendMessage(userSaved).setEphemeral(true).queue();
            } else {
                String youAlreadyTracked = jsonParsers.getTranslation("you_already_tracked", guildIdString);
                event.getHook().sendMessage(youAlreadyTracked).setEphemeral(true).queue();
            }
            return;
        }

        if (Objects.equals(event.getButton().getId(), BUTTON_ALL_USERS)) {
            event.editButton(event.getButton().asDisabled()).queue();
            List<User> members = event.getMessage().getMentions().getUsers();
            List<Subs> allSubs = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildIdLong);
            List<User> collect = members
                    .stream()
                    .filter(a -> allSubs.stream()
                            .map(Subs::getUserTrackingId)
                            .noneMatch(s -> s.contains(a.getId())))
                    .filter(m -> !BotStartConfig.mapLocks.containsKey(m.getId()))
                    .collect(Collectors.toList());

            List<Subs> subsList = new ArrayList<>();
            Server serverId = guildRepository.getReferenceById(event.getGuild().getIdLong());

            for (User value : collect) {
                Subs subs = new Subs();
                subs.setServer(serverId);
                subs.setUserTrackingId(value.getId());
                subs.setUserId(user.getIdLong());

                subsList.add(subs);
            }
            noticeRepository.saveAll(subsList);
            String usersSaved = jsonParsers.getTranslation("users_saved", guildIdString);
            event.getHook().sendMessage(usersSaved).setEphemeral(true).queue();
            return;
        }

    }
}