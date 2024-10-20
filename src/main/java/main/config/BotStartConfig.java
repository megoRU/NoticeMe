package main.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import main.controller.UpdateController;
import main.core.CoreBot;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Language;
import main.model.entity.Lock;
import main.model.entity.Server;
import main.model.entity.Subs;
import main.model.repository.GuildRepository;
import main.model.repository.LanguageRepository;
import main.model.repository.LockRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.boticordjava.api.entity.bot.stats.BotStats;
import org.boticordjava.api.impl.BotiCordAPI;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class BotStartConfig {

    public static final String activity = "/sub | ";
    private final static Logger LOGGER = Logger.getLogger(BotStartConfig.class.getName());

    private static final ConcurrentMap<String, Language.LanguageEnum> mapLanguages = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Lock.Locked> mapLocks = new ConcurrentHashMap<>();

    public static JDA jda;
    private final JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getTOKEN());

    //REPOSITORY
    private final NoticeRepository noticeRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;
    private final GuildRepository guildRepository;

    private final UpdateController updateController;

    @PostConstruct
    private void startBot() {
        try {
            //Update
            setLanguages();
            getLanguages();
            getAllServers();
            getLockStatus();
            getAllUsers();

            List<GatewayIntent> intents = new ArrayList<>(
                    Arrays.asList(
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGES));

            jdaBuilder.disableCache(
                    CacheFlag.ONLINE_STATUS,
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.MEMBER_OVERRIDES,
                    CacheFlag.ACTIVITY,
                    CacheFlag.EMOJI,
                    CacheFlag.STICKER,
                    CacheFlag.ROLE_TAGS,
                    CacheFlag.FORUM_TAGS,
                    CacheFlag.SCHEDULED_EVENTS
            );

            jdaBuilder.setAutoReconnect(true);
            jdaBuilder.setStatus(OnlineStatus.ONLINE);
            jdaBuilder.enableIntents(intents);
            jdaBuilder.setActivity(Activity.playing("Starting..."));
            jdaBuilder.setBulkDeleteSplittingEnabled(false);
            jdaBuilder.addEventListeners(new CoreBot(updateController));

            jda = jdaBuilder.build();
            jda.awaitReady();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Starting Bot error", e);
        }

        List<Command> complete = jda.retrieveCommands().complete();
        complete.forEach(command -> System.out.println(command.toString()));

        //Обновить команды
        updateSlashCommands();
        System.out.println("14:42");
    }

    private void updateSlashCommands() {
        try {
            CommandListUpdateAction commands = jda.updateCommands();

            List<OptionData> language = new ArrayList<>();

            language.add(new OptionData(STRING, "bot", "Setting the bot language")
                    .addChoice("english", Language.LanguageEnum.EN.name())
                    .addChoice("russian", Language.LanguageEnum.RU.name())
                    .setRequired(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка языка бота"));

            List<OptionData> setup = new ArrayList<>();

            setup.add(new OptionData(CHANNEL, "text-channel", "Select TextChannel for notification")
                    .setRequired(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Выберите текстовый канал для уведомления"));

            List<OptionData> notifications = new ArrayList<>();

            notifications.add(new OptionData(USER, "user", "Select a user to track")
                    .setRequired(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Выбрать пользователя для отслеживания"));

            List<OptionData> unsub = new ArrayList<>();

            unsub.add(new OptionData(USER, "user", "Unsubscribe from a user")
                    .setRequired(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя"));

            List<OptionData> unsub_v2 = new ArrayList<>();

            unsub_v2.add(new OptionData(STRING, "user_id", "Unsubscribe from a user by User ID")
                    .setRequired(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя по User ID"));

            /*
             * Команды
             */

            CommandData languageCommand = Commands.slash("language", "Setting language")
                    .setGuildOnly(true)
                    .addOptions(language)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка языка");

            CommandData helpCommand = Commands.slash("help", "Bot commands")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Команды бота");

            CommandData donateCommand = Commands.slash("donate", "Send a donation")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отправить пожертвование");

            CommandData setupCommand = Commands.slash("setup", "Set up a TextChannel for notifications")
                    .setGuildOnly(true)
                    .addOptions(setup)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка текстового канала для уведомлений");

            CommandData subCommand = Commands.slash("sub", "Subscribe to user")
                    .setGuildOnly(true)
                    .addOptions(notifications)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Подписаться на пользователя");

            CommandData listCommand = Commands.slash("list", "List of your subscriptions")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Список твоих подписок");

            CommandData unsubCommand = Commands.slash("unsub", "Unsubscribe from the user")
                    .setGuildOnly(true)
                    .addOptions(unsub)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя");

            CommandData unsubV2Command = Commands.slash("unsub-v2", "Unsubscribe from the user")
                    .setGuildOnly(true)
                    .addOptions(unsub_v2)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя");

            CommandData checkCommand = Commands.slash("check", "Checking the bot's permissions")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Проверка разрешений бота");

            CommandData deleteCommand = Commands.slash("delete", "Delete all subscribers from the server")
                    .setGuildOnly(true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Удалить всех подписчиков с сервера");

            CommandData suggestionCommand = Commands.slash("suggestion", "List of suggestions for tracking users")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Список из предложений к отслеживанию пользователей");

            CommandData lockCommand = Commands.slash("lock", "Forbid tracking yourself on all servers")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Запретить отслеживать себя на всех серверах");

            CommandData unlockCommand = Commands.slash("unlock", "Allow tracking yourself on all servers")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Разрешить отслеживать себя на всех серверах");

            commands.addCommands(
                            languageCommand,
                            helpCommand,
                            donateCommand,
                            setupCommand,
                            subCommand,
                            listCommand,
                            unsubCommand,
                            unsubV2Command,
                            checkCommand,
                            deleteCommand,
                            suggestionCommand,
                            lockCommand,
                            unlockCommand)
                    .queue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Slash error", e);
        }
    }

    @Scheduled(fixedDelay = 900000L, initialDelay = 8000L)
    private void topGG() {
        if (!Config.isIsDev()) {
            try {
                if (System.getenv("BOTICORD") == null) return;
                int serverCount = BotStartConfig.jda.getGuilds().size();
                jda.getPresence().setActivity(Activity.playing(BotStartConfig.activity + serverCount + " guilds"));

                int countMembers = jda.getGuilds().stream()
                        .map(Guild::getMembers)
                        .mapToInt(Collection::size)
                        .sum();

                BotStats botStats = new BotStats(countMembers, serverCount, 1);

                BotiCordAPI api = new BotiCordAPI.Builder()
                        .token(System.getenv("BOTICORD"))
                        .build();

                api.setBotStats(Config.getBotId(), botStats);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "topGG", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void setLanguages() {
        try {
            List<String> listLanguages = new ArrayList<>();
            listLanguages.add("rus");
            listLanguages.add("eng");

            for (String listLanguage : listLanguages) {
                InputStream inputStream = new ClassPathResource("json/" + listLanguage + ".json").getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                JSONObject jsonObject = new JSONObject(new JSONTokener(reader));

                for (String o : jsonObject.keySet()) {
                    if (listLanguage.equals("rus")) {
                        ParserClass.russian.put(o, String.valueOf(jsonObject.get(o)));
                    } else {
                        ParserClass.english.put(o, String.valueOf(jsonObject.get(o)));
                    }
                }
                reader.close();
                inputStream.close();
                reader.close();
            }
            System.out.println("setLanguages()");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "setLanguages", e);
        }
    }

    private void getLanguages() {
        try {
            List<Language> languageList = languageRepository.findAll();
            for (Language language : languageList) {
                mapLanguages.put(language.getGuildId().toString(), language.getLanguage());
            }
            System.out.println("getLanguages()");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "getLanguages", e);
        }
    }

    private void getLockStatus() {
        try {
            List<Lock> lockList = lockRepository.findAll();
            for (Lock lock : lockList) {
                if (lock.getLocked().equals(Lock.Locked.LOCKED)) {
                    mapLocks.put(lock.getUserId().toString(), Lock.Locked.LOCKED);
                }
            }
            System.out.println("getLockStatus()");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "getLockStatus", e);
        }
    }

    private void getAllServers() {
        try {
            NoticeRegistry instance = NoticeRegistry.getInstance();
            List<Server> serverList = guildRepository.findAll();
            for (Server server : serverList) {
                String serverId = server.getGuildIdLong().toString();
                instance.putServer(serverId, server);
            }
            System.out.println("getAllServers()");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "getAllServers", e);
        }
    }

    private void getAllUsers() {
        try {
            List<Subs> noticeList = noticeRepository.findAll();
            NoticeRegistry instance = NoticeRegistry.getInstance();

            for (Subs notice : noticeList) {
                String guildId = notice.getServer().getGuildIdLong().toString();
                String userIdTracker = notice.getUserTrackingId();
                String userId = notice.getUserId().toString();
                instance.sub(guildId, userId, userIdTracker);
            }
            System.out.println("getAllUsers()");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "getAllUsers", e);
        }
    }

    public static Map<String, Language.LanguageEnum> getMapLanguages() {
        return mapLanguages;
    }

    public static Map<String, Lock.Locked> getMapLocks() {
        return mapLocks;
    }
}