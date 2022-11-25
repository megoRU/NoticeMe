package main.config;

import main.core.NoticeRegistry;
import main.core.TrackingUser;
import main.event.BotJoinToGuild;
import main.event.UserJoinEvent;
import main.event.buttons.ButtonEvent;
import main.event.slash.SlashCommandEvent;
import main.jsonparser.ParserClass;
import main.model.entity.Language;
import main.model.entity.Lock;
import main.model.entity.Subs;
import main.model.repository.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

@Configuration
@EnableScheduling
public class BotStartConfig {

    public static final String activity = "/help | ";
    public static final ConcurrentMap<String, Language.LanguageEnum> mapLanguages = new ConcurrentHashMap<>();
    public static final ConcurrentMap<String, Lock.Locked> mapLocks = new ConcurrentHashMap<>();
    public static JDA jda;
    private final JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getTOKEN());

    //API
//    private final DiscordBotListAPI TOP_GG_API = new DiscordBotListAPI.Builder()
//            .token(Config.getTopGgApiToken())
//            .botId(Config.getBotId())
//            .build();

//    private final BotiCordAPI api = new BotiCordAPI.Builder()
//            .tokenEnum(TokenEnum.BOT)
//            .token(System.getenv("BOTICORD"))
//            .build();

    //REPOSITORY
    private final NoticeRepository noticeRepository;
    private final GuildRepository guildRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;
    private final EntriesRepository entriesRepository;

    //DataBase
//    @Value("${spring.datasource.url}")
//    private String URL_CONNECTION;
//    @Value("${spring.datasource.username}")
//    private String USER_CONNECTION;
//    @Value("${spring.datasource.password}")
//    private String PASSWORD_CONNECTION;

    @Autowired
    public BotStartConfig(NoticeRepository noticeRepository,
                          GuildRepository guildRepository,
                          LanguageRepository languageRepository,
                          LockRepository lockRepository,
                          EntriesRepository entriesRepository) {
        this.noticeRepository = noticeRepository;
        this.guildRepository = guildRepository;
        this.languageRepository = languageRepository;
        this.lockRepository = lockRepository;
        this.entriesRepository = entriesRepository;
    }

    @Bean
    public void startBot() {
        try {
            //Update
            setLanguages();
            getLanguages();
            getLockStatus();
            getAllUsers();

            List<GatewayIntent> intents = new ArrayList<>(
                    Arrays.asList(
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGES));

            jdaBuilder.disableCache(
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.ACTIVITY,
                    CacheFlag.MEMBER_OVERRIDES,
                    CacheFlag.ONLINE_STATUS);

            jdaBuilder.setAutoReconnect(true);
            jdaBuilder.setStatus(OnlineStatus.ONLINE);
            jdaBuilder.enableIntents(intents);
            jdaBuilder.setActivity(Activity.playing("Starting..."));
            jdaBuilder.setBulkDeleteSplittingEnabled(false);
            jdaBuilder.addEventListeners(new SlashCommandEvent(noticeRepository, guildRepository, languageRepository, lockRepository, entriesRepository));
            jdaBuilder.addEventListeners(new UserJoinEvent(guildRepository, entriesRepository));
            jdaBuilder.addEventListeners(new BotJoinToGuild());
            jdaBuilder.addEventListeners(new ButtonEvent(guildRepository, noticeRepository));

            jda = jdaBuilder.build();
            jda.awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(jda.retrieveCommands().complete());

        //Обновить команды
//        updateSlashCommands();
        System.out.println("17:25");
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

            commands.addCommands(Commands.slash("language", "Setting language")
                    .setGuildOnly(true)
                    .addOptions(language)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка языка"));

            commands.addCommands(Commands.slash("help", "Bot commands")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Команды бота"));

            commands.addCommands(Commands.slash("setup", "Set up a TextChannel for notifications")
                    .setGuildOnly(true)
                    .addOptions(setup)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка текстового канала для уведомлений"));

            commands.addCommands(Commands.slash("sub", "Subscribe to user")
                    .setGuildOnly(true)
                    .addOptions(notifications)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Подписаться на пользователя"));

            commands.addCommands(Commands.slash("list", "List of your subscriptions")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Список твоих подписок"));

            commands.addCommands(Commands.slash("unsub", "Unsubscribe from the user")
                    .setGuildOnly(true)
                    .addOptions(notifications)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя"));

            commands.addCommands(Commands.slash("delete", "Delete all subscribers from the server")
                    .setGuildOnly(true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Удалить всех подписчиков с сервера"));

            commands.addCommands(Commands.slash("suggestion", "List of suggestions for tracking users")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Список из предложений к отслеживанию пользователей"));

            commands.queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 900000L, initialDelay = 8000L)
    private void topGG() {
        if (!Config.isIsDev()) {
            try {
                int serverCount = BotStartConfig.jda.getGuilds().size();
//                TOP_GG_API.setStats(serverCount);
                BotStartConfig.jda.getPresence().setActivity(Activity.playing(BotStartConfig.activity + serverCount + " guilds"));

//                AtomicInteger usersCount = new AtomicInteger();
//                jda.getGuilds().forEach(g -> usersCount.addAndGet(g.getMembers().size()));

//                try {
//                    api.setStats(serverCount, 1, usersCount.get());
//                } catch (UnsuccessfulHttpException un) {
//                    System.out.println(un.getMessage());
//                }
            } catch (Exception e) {
                e.printStackTrace();
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
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(reader);

                for (Object o : jsonObject.keySet()) {
                    String key = (String) o;

                    if (listLanguage.equals("rus")) {
                        ParserClass.russian.put(key, String.valueOf(jsonObject.get(key)));
                    } else {
                        ParserClass.english.put(key, String.valueOf(jsonObject.get(key)));
                    }
                }
                reader.close();
                inputStream.close();
                reader.close();
            }
            System.out.println("setLanguages()");
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void getAllUsers() {
        List<Subs> noticeList = noticeRepository.findAll();
        NoticeRegistry instance = NoticeRegistry.getInstance();

        for (Subs notice : noticeList) {
            String guildId = notice.getServer().getGuildIdLong().toString();
            String userIdTracker = notice.getUserTrackingId().toString();
            String userId = notice.getUserId().toString();
            if (!instance.hasGuild(guildId)) {
                TrackingUser trackingUser = new TrackingUser();
                trackingUser.putUser(notice.getUserId().toString());
                ConcurrentMap<String, TrackingUser> trackingUserConcurrentMap = new ConcurrentHashMap<>();
                trackingUserConcurrentMap.put(userIdTracker, trackingUser);
                instance.save(guildId, trackingUserConcurrentMap);
            } else {
                TrackingUser trackingUserFromMap = instance.getUser(guildId, userIdTracker);
                if (trackingUserFromMap == null) {
                    TrackingUser trackingUser = new TrackingUser();
                    trackingUser.putUser(notice.getUserId().toString());
                    instance.saveTrackingUser(guildId, userIdTracker, trackingUser);
                } else {
                    instance.get(guildId).get(userIdTracker).putUser(userId);
                }
            }
        }

    }
}
