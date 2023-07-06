package main.config;

import main.controller.UpdateController;
import main.core.CoreBot;
import main.core.core.NoticeRegistry;
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.boticordjava.api.entity.bot.stats.BotStats;
import org.boticordjava.api.impl.BotiCordAPI;
import org.boticordjava.api.io.UnsuccessfulHttpException;
import org.discordbots.api.client.DiscordBotListAPI;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

@Configuration
@EnableScheduling
public class BotStartConfig {

    public static final String activity = "/help | ";
    private static final ConcurrentMap<String, Language.LanguageEnum> mapLanguages = new ConcurrentHashMap<>();
    public static final ConcurrentMap<String, Lock.Locked> mapLocks = new ConcurrentHashMap<>();
    public static JDA jda;
    private final JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getTOKEN());

    //API
    private final DiscordBotListAPI TOP_GG_API = new DiscordBotListAPI.Builder()
            .token(Config.getTopGgApiToken())
            .botId(Config.getBotId())
            .build();

    private final BotiCordAPI api = new BotiCordAPI.Builder()
            .token(System.getenv("BOTICORD"))
            .build();

    //REPOSITORY
    private final NoticeRepository noticeRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;

    private final UpdateController updateController;

    @Autowired
    public BotStartConfig(NoticeRepository noticeRepository,
                          LanguageRepository languageRepository,
                          LockRepository lockRepository,
                          UpdateController updateController) {
        this.noticeRepository = noticeRepository;
        this.languageRepository = languageRepository;
        this.lockRepository = lockRepository;
        this.updateController = updateController;
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
            jdaBuilder.addEventListeners(new CoreBot(updateController));

            jda = jdaBuilder.build();
            jda.awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Command> complete = jda.retrieveCommands().complete();
        complete.forEach(command -> System.out.println(command.toString()));

        //Обновить команды
//        updateSlashCommands();
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

            commands.addCommands(Commands.slash("language", "Setting language")
                    .setGuildOnly(true)
                    .addOptions(language)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка языка"));

            commands.addCommands(Commands.slash("help", "Bot commands")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Команды бота"));

            commands.addCommands(Commands.slash("donate", "Send a donation")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отправить пожертвование"));

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

            commands.addCommands(Commands.slash("lock", "Forbid tracking yourself on all servers")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Запретить отслеживать себя на всех серверах"));

            commands.addCommands(Commands.slash("unlock", "Allow tracking yourself on all servers")
                    .setGuildOnly(true)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Разрешить отслеживать себя на всех серверах"));

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
                TOP_GG_API.setStats(serverCount);
                jda.getPresence().setActivity(Activity.playing(BotStartConfig.activity + serverCount + " guilds"));

                int countMembers = jda.getGuilds().stream()
                        .map(Guild::getMembers)
                        .mapToInt(Collection::size)
                        .sum();
                try {
                    BotStats botStats = new BotStats(countMembers, serverCount, 1);
                    api.setBotStats(Config.getBotId(), botStats);
                } catch (UnsuccessfulHttpException un) {
                    un.printStackTrace();
                }
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
            e.printStackTrace();
        }
    }

    public static Map<String, Language.LanguageEnum> getMapLanguages() {
        return mapLanguages;
    }

}
