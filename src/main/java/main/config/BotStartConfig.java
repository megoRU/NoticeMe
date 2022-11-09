package main.config;

import main.core.NoticeRegistry;
import main.core.TrackingUser;
import main.event.UserJoinEvent;
import main.jsonparser.ParserClass;
import main.model.entity.Language;
import main.model.entity.Notice;
import main.model.repository.GuildRepository;
import main.model.repository.NoticeRepository;
import main.slash.SlashCommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    //DataBase
    @Value("${spring.datasource.url}")
    private String URL_CONNECTION;
    @Value("${spring.datasource.username}")
    private String USER_CONNECTION;
    @Value("${spring.datasource.password}")
    private String PASSWORD_CONNECTION;

    @Autowired
    public BotStartConfig(NoticeRepository noticeRepository, GuildRepository guildRepository) {
        this.noticeRepository = noticeRepository;
        this.guildRepository = guildRepository;
    }

    @Bean
    public void startBot() {
        try {
            //
            setLanguages();
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
            jdaBuilder.addEventListeners(new SlashCommandEvent(noticeRepository, guildRepository));
            jdaBuilder.addEventListeners(new UserJoinEvent(guildRepository));

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
                    .setRequired(true));

            List<OptionData> setup = new ArrayList<>();

            setup.add(new OptionData(CHANNEL, "text-channel", "Setting TextChannel for notification")
                    .setRequired(true));

            List<OptionData> notifications = new ArrayList<>();

            notifications.add(new OptionData(USER, "user", "Select a user to track")
                    .setRequired(true));

            commands.addCommands(Commands.slash("language", "Setting language")
                    .setGuildOnly(true)
                    .addOptions(language));

            commands.addCommands(Commands.slash("help", "Bot commands")
                    .setGuildOnly(true));

            commands.addCommands(Commands.slash("setup", "Set up a TextChannel for notifications")
                    .setGuildOnly(true)
                    .addOptions(setup));

            commands.addCommands(Commands.slash("notice", "Configure Notifications")
                    .setGuildOnly(true)
                    .addOptions(notifications));

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
            String[] languages = {"rus", "eng"};

            for (String listLanguage : languages) {
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

    private void getAllUsers() {
        List<Notice> noticeList = noticeRepository.findAll();
        NoticeRegistry instance = NoticeRegistry.getInstance();

        for (Notice notice : noticeList) {
            String guildId = notice.getGuildId().getGuildId().toString();
            String userIdTracker = notice.getUserTrackingId().toString();
            String userId = notice.getUserId().toString();

            if (!instance.getTrackingUserConcurrentMap().containsKey(guildId)) {
                TrackingUser trackingUser = new TrackingUser();
                trackingUser.putUser(notice.getUserId().toString());

                ConcurrentMap<String, TrackingUser> trackingUserConcurrentMap = new ConcurrentHashMap<>();
                trackingUserConcurrentMap.put(userIdTracker, trackingUser);

                instance.getTrackingUserConcurrentMap().put(guildId, trackingUserConcurrentMap);

                System.out.println(instance.getTrackingUserConcurrentMap().get(guildId).size());
            } else {
                ConcurrentMap<String, TrackingUser> trackingUserConcurrentMap = instance.getTrackingUserConcurrentMap().get(guildId);
                System.out.println(trackingUserConcurrentMap.size());

                if (trackingUserConcurrentMap.get(userIdTracker) == null) {

                    TrackingUser trackingUser = new TrackingUser();
                    trackingUser.putUser(notice.getUserId().toString());

                    trackingUserConcurrentMap.put(userIdTracker, trackingUser);

                } else {
                    trackingUserConcurrentMap.get(userIdTracker).putUser(userId);
                }
            }
        }

    }
}
