package main.event.slash;

import lombok.RequiredArgsConstructor;
import main.config.BotStartConfig;
import main.core.NoticeRegistry;
import main.event.buttons.ButtonEvent;
import main.jsonparser.ParserClass;
import main.model.entity.*;
import main.model.repository.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SlashCommandEvent extends ListenerAdapter {

    //REPO
    private final NoticeRepository noticeRepository;
    private final GuildRepository guildRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;
    private final EntriesRepository entriesRepository;

    //Language
    private static final ParserClass jsonParsers = new ParserClass();

    //LOGGER
    private final static Logger LOGGER = Logger.getLogger(SlashCommandEvent.class.getName());

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;
        if (event.getGuild() == null) return;

        User user = event.getUser();
        long guildId = event.getGuild().getIdLong();
        String guildIdString = event.getGuild().getId();

        LOGGER.info(String.format("\nSlash Command name: %s", event.getName()));

        if (event.getName().equals("setup")) {
            GuildChannelUnion guildChannelUnion = event.getOption("text-channel", OptionMapping::getAsChannel);
            if (guildChannelUnion instanceof TextChannel) {
                Server guild = new Server();
                guild.setGuildIdLong(guildId);
                guild.setTextChannelId(guildChannelUnion.asTextChannel().getIdLong()); //NPE
                guildRepository.save(guild);

                System.out.println(jsonParsers.getTranslation("now_bot_will_receive", guildIdString));

                String nowBotWillReceive = String.format(
                        jsonParsers.getTranslation("now_bot_will_receive", guildIdString),
                        guildChannelUnion.asTextChannel().getIdLong());
                event.reply(nowBotWillReceive).queue();
            } else if (guildChannelUnion instanceof NewsChannel) {
                event.reply("It can't be a channel NewsChannel").queue();
                return;
            }
            return;
        }

        if (event.getName().equals("sub")) {
            User userDest = event.getOption("user", OptionMapping::getAsUser);
            if (userDest == null) {
                event.reply("user is null").queue();
                return;
            } else if (userDest.getIdLong() == user.getIdLong()) {
                String yourself = jsonParsers.getTranslation("yourself", guildIdString);
                event.reply(yourself).queue();
                return;
            } else if (user.isBot()) {
                String bot = jsonParsers.getTranslation("bot", guildIdString);
                event.reply(bot).queue();
                return;
            }

            if (BotStartConfig.mapLocks.containsKey(userDest.getId())) {
                String cannotSubToThisUser = jsonParsers.getTranslation("cannot_sub_to_this_user", guildIdString);
                event.reply(cannotSubToThisUser).setEphemeral(true).queue();
                return;
            }

            Optional<Server> guildOptional = guildRepository.findById(guildId);
            if (guildOptional.isPresent()) {
                Subs notice = new Subs();
                notice.setServer(guildOptional.get());
                notice.setUserId(user.getIdLong());
                notice.setUserTrackingId(userDest.getId());
                noticeRepository.save(notice);

                NoticeRegistry instance = NoticeRegistry.getInstance();
                instance.save(guildIdString, user.getId(), userDest.getId());

                String nowYouWillReceive = String.format(jsonParsers.getTranslation("now_you_will_receive", guildIdString), userDest.getIdLong());
                event.reply(nowYouWillReceive).setEphemeral(true).queue();
            } else {
                String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);
                event.reply(youCannotSetChannel).setEphemeral(true).queue();
            }
            return;
        }

        if (event.getName().equals("language")) {
            String languageAsString = event.getOption("bot", OptionMapping::getAsString);
            if (languageAsString == null) return;

            Language.LanguageEnum languageEnum = Language.LanguageEnum.valueOf(languageAsString);

            Language language = new Language();
            language.setGuildId(guildId);
            language.setLanguage(languageEnum);

            languageRepository.save(language);

            BotStartConfig.mapLanguages.put(guildIdString, languageEnum);
            String languageSet = jsonParsers.getTranslation("language_set", guildIdString);
            event.reply(languageSet).queue();
            return;
        }

        //TODO: Возможно лучше искать локально в коллекциях
        if (event.getName().equals("list")) {
            List<Subs> noticeList = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildId);
            if (!noticeList.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();

                for (Subs notice : noticeList) {
                    if (stringBuilder.isEmpty()) {
                        stringBuilder.append("<@").append(notice.getUserTrackingId()).append(">");
                    } else {
                        stringBuilder.append(", <@").append(notice.getUserTrackingId()).append(">");
                    }
                }
                String subscription = String.format(jsonParsers.getTranslation("subscription", guildIdString), stringBuilder);
                event.reply(subscription).setEphemeral(true).queue();
            } else {
                String emptyList = jsonParsers.getTranslation("empty_list", guildIdString);
                event.reply(emptyList).setEphemeral(true).queue();
            }
            return;
        }

        if (event.getName().equals("unsub")) {
            User userFromOptions = event.getOption("user", OptionMapping::getAsUser);
            if (userFromOptions == null) return;
            Subs notice = noticeRepository.findTrackingUser(user.getIdLong(), guildId, userFromOptions.getId());

            if (notice == null) {
                String dontFindUser = jsonParsers.getTranslation("dont_find_user", guildIdString);
                event.reply(dontFindUser).queue();
            } else {
                noticeRepository.deleteByUserTrackingId(notice.getUserTrackingId(), user.getIdLong());

                NoticeRegistry instance = NoticeRegistry.getInstance();
                instance.unsub(guildIdString, userFromOptions.getId(), user.getId());

                String successfullyDeleted = String.format(jsonParsers.getTranslation("successfully_deleted", guildIdString), notice.getUserTrackingId());
                event.reply(successfullyDeleted).setEphemeral(true).queue();
            }
            return;
        }

        if (event.getName().equals("delete")) {
            String warningDeleteData = jsonParsers.getTranslation("warning_delete_data", guildIdString);
            event.reply(warningDeleteData)
                    .setEphemeral(true)
                    .setActionRow(Button.danger(ButtonEvent.BUTTON_DELETE, "Delete"))
                    .queue();
            return;
        }

        if (event.getName().equals("lock")) {
            BotStartConfig.mapLocks.put(user.getId(), Lock.Locked.LOCKED);
            String lockString = jsonParsers.getTranslation("lock", guildIdString);
            event.reply(lockString).setEphemeral(true).queue();

            NoticeRegistry instance = NoticeRegistry.getInstance();
            instance.removeUserFromAllGuild(user.getId());

            noticeRepository.deleteAllByUserTrackingId(user.getId());

            Lock lock = new Lock();
            lock.setUserId(user.getIdLong());
            lock.setLocked(Lock.Locked.LOCKED);
            lockRepository.save(lock);
            return;
        }

        if (event.getName().equals("unlock")) {
            BotStartConfig.mapLocks.remove(user.getId());
            String unLockString = jsonParsers.getTranslation("unlock", guildIdString);
            event.reply(unLockString).setEphemeral(true).queue();
            lockRepository.deleteById(user.getIdLong());
            return;
        }

        if (event.getName().equals("suggestion")) {
            List<Entries> allEntriesForSuggestion = entriesRepository.getAllEntriesForSuggestion(user.getIdLong(), guildId);
            List<Subs> allSubs = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildId);

            String collect = allEntriesForSuggestion
                    .stream()
                    .map(Entries::getUsersInChannel)
                    .distinct()
                    .filter(a -> allSubs.stream()
                            .map(Subs::getUserTrackingId)
                            .noneMatch(s -> s.contains(a)))
                    .collect(Collectors.joining(","));

            List<String> stringList =
                    Arrays.stream(collect.split(","))
                            .filter(m -> !BotStartConfig.mapLocks.containsKey(m))
                            .collect(Collectors.toList());

            if (!collect.isEmpty() && !stringList.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                List<Button> buttonsList = new ArrayList<>();

                for (int i = 0; i < stringList.size(); i++) {
                    if (stringBuilder.isEmpty()) {
                        stringBuilder.append((i + 1)).append(". ").append("<@").append(stringList.get(i)).append(">");
                    } else {
                        stringBuilder.append("\n").append((i + 1)).append(". ").append("<@").append(stringList.get(i)).append(">");
                    }
                    if (buttonsList.size() <= 23) {
                        String addUser = String.format(jsonParsers.getTranslation("add_user", guildIdString), (i + 1));
                        buttonsList.add(Button.primary(ButtonEvent.BUTTON_ADD_USER + (i + 1), addUser));
                    }
                }

                if (buttonsList.size() > 1) {
                    String addAll = jsonParsers.getTranslation("add_all", guildIdString);
                    buttonsList.add(Button.success(ButtonEvent.BUTTON_ALL_USERS, addAll));
                }

                String suggestionText = String.format(jsonParsers.getTranslation("suggestion_text", guildIdString), stringBuilder);
                ReplyCallbackAction replyCallbackAction = event.reply(suggestionText).setEphemeral(true);

                int second = Math.min(buttonsList.size(), 4);
                int first = 0;
                int ceil = (int) Math.ceil(buttonsList.size() / 5.0);
                for (int i = 0; i < ceil; i++) {
                    replyCallbackAction.addActionRow(buttonsList.subList(first, second));
                    first = second;
                    second += 4;
                }
                replyCallbackAction.queue();
            } else {
                String noSuggestions = jsonParsers.getTranslation("no_suggestions", guildIdString);
                event.reply(noSuggestions).setEphemeral(true).queue();
            }
            return;
        }

    }
}