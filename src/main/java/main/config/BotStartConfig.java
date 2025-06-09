package main.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import main.controller.UpdateController;
import main.core.CoreBot;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.*;
import main.model.repository.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class BotStartConfig {

    public static final String activity = "/sub | ";
    private final static Logger LOGGER = LoggerFactory.getLogger(BotStartConfig.class.getName());

    private static final ConcurrentMap<String, Language.LanguageEnum> mapLanguages = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Lock.Locked> mapLocks = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Gender.GenderType> mapGenders = new ConcurrentHashMap<>();
    private final static Map<String, Long> commandMap = new HashMap<>();

    public static JDA jda;
    private final JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getTOKEN());

    //REPOSITORY
    private final NoticeRepository noticeRepository;
    private final SuggestionsRepository suggestionsRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;
    private final GuildRepository guildRepository;
    private final GenderRepository genderRepository;

    private final UpdateController updateController;

    @PostConstruct
    private void startBot() {
        try {
            CoreBot coreBot = new CoreBot(updateController);

            //Update
            setLanguages();
            getLanguages();
            getGenders();
            getAllServers();
            getLockStatus();
            getAllUsers();
            getSuggestions();

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
            jdaBuilder.addEventListeners(coreBot);

            jda = jdaBuilder.build();
            jda.awaitReady();

            //Обновить команды
            updateSlashCommands();

            jda.retrieveCommands().queue(
                    list -> {
                        for (Command command : list) {
                            String name = command.getName();
                            long id = command.getIdLong();
                            commandMap.put(name, id);
                            System.out.printf("%s [%s]%n", id, name);
                        }
                    }
            );

            System.out.println("15:16");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void updateSlashCommands() {
        try {
            CommandListUpdateAction commands = jda.updateCommands();

            List<OptionData> language = new ArrayList<>();

            language.add(new OptionData(STRING, "bot", "Setting the bot language")
                    .addChoice("English", Language.LanguageEnum.EN.name())
                    .addChoice("Русский", Language.LanguageEnum.RU.name())
                    .addChoice("Français", Language.LanguageEnum.FR.name())
                    .setRequired(true)
                    .setNameLocalization(DiscordLocale.RUSSIAN, "бот")
                    .setNameLocalization(DiscordLocale.FRENCH, "bot")
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка языка бота")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Définition de la langue du bot"));

            List<OptionData> gender = new ArrayList<>();

            gender.add(new OptionData(STRING, "gender", "Setting your gender")
                    .addChoice("Male", Gender.GenderType.MALE.name())
                    .addChoice("Female", Gender.GenderType.FEMALE.name())
                    .setRequired(true)
                    .setNameLocalization(DiscordLocale.RUSSIAN, "гендер")
                    .setNameLocalization(DiscordLocale.FRENCH, "genre")
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка своего гендера")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Définir son genre"));

            List<OptionData> setup = new ArrayList<>();

            setup.add(new OptionData(CHANNEL, "text-channel", "Select TextChannel for notification")
                    .setRequired(true)
                    .setNameLocalization(DiscordLocale.RUSSIAN, "текстовый-канал")
                    .setNameLocalization(DiscordLocale.FRENCH, "canal-texte")
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Выберите текстовый канал для уведомления")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Sélectionnez un canal texte pour les notifications"));

            List<OptionData> notifications = new ArrayList<>();

            notifications.add(new OptionData(USER, "user", "Select a user to track")
                    .setRequired(true)
                    .setNameLocalization(DiscordLocale.RUSSIAN, "пользователь")
                    .setNameLocalization(DiscordLocale.FRENCH, "utilisateur")
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Выбрать пользователя для отслеживания")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Sélectionner un utilisateur à suivre"));

            List<OptionData> unsub = new ArrayList<>();

            unsub.add(new OptionData(USER, "user", "Unsubscribe from a user")
                    .setRequired(true)
                    .setNameLocalization(DiscordLocale.RUSSIAN, "пользователь")
                    .setNameLocalization(DiscordLocale.FRENCH, "utilisateur")
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Se désabonner d'un utilisateur"));

            List<OptionData> unsubV2 = new ArrayList<>();

            unsubV2.add(new OptionData(STRING, "user-id", "Unsubscribe from a user by User ID")
                    .setRequired(true)
                    .setNameLocalization(DiscordLocale.RUSSIAN, "id-пользователя")
                    .setNameLocalization(DiscordLocale.FRENCH, "id-utilisateur")
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя по User ID")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Se désabonner par identifiant utilisateur"));

            /*
             * Команды
             */

            CommandData genderCommand = Commands.slash("gender", "Setting your gender")
                    .setContexts(InteractionContextType.GUILD)
                    .addOptions(gender)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка своего гендера")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Définir son genre");

            CommandData languageCommand = Commands.slash("language", "Setting language")
                    .setContexts(InteractionContextType.GUILD)
                    .addOptions(language)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка языка")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Définir la langue");

            CommandData helpCommand = Commands.slash("help", "Bot commands")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Команды бота")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Commandes du bot");

            CommandData donateCommand = Commands.slash("donate", "Send a donation")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отправить пожертвование")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Envoyer un don");

            CommandData setupCommand = Commands.slash("setup", "Set up a TextChannel for notifications")
                    .setContexts(InteractionContextType.GUILD)
                    .addOptions(setup)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Установка текстового канала для уведомлений")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Configurer un canal texte pour les notifications");

            CommandData subCommand = Commands.slash("sub", "Subscribe to user")
                    .setContexts(InteractionContextType.GUILD)
                    .addOptions(notifications)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Подписаться на пользователя")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "S'abonner à un utilisateur");

            CommandData listCommand = Commands.slash("list", "List of your subscriptions")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Список твоих подписок")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste de vos abonnements");

            CommandData unsubCommand = Commands.slash("unsub", "Unsubscribe from the user")
                    .setContexts(InteractionContextType.GUILD)
                    .addOptions(unsub)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Se désabonner de l'utilisateur");

            CommandData unsubV2Command = Commands.slash("unsub-v2", "Unsubscribe from the user")
                    .setContexts(InteractionContextType.GUILD)
                    .addOptions(unsubV2)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Отписаться от пользователя")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Se désabonner de l'utilisateur");

            CommandData checkCommand = Commands.slash("check", "Checking the bot's permissions")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Проверка разрешений бота")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Vérification des permissions du bot");

            CommandData deleteCommand = Commands.slash("delete", "Delete all subscribers from the server")
                    .setContexts(InteractionContextType.GUILD)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Удалить всех подписчиков с сервера")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Supprimer tous les abonnés du serveur");

            CommandData suggestionCommand = Commands.slash("suggestion", "List of suggestions for tracking users")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Список из предложений к отслеживанию пользователей")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste des suggestions d'utilisateurs à suivre");

            CommandData lockCommand = Commands.slash("lock", "Forbid tracking yourself on all servers")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Запретить отслеживать себя на всех серверах")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Interdire le suivi de soi sur tous les serveurs");

            CommandData unlockCommand = Commands.slash("unlock", "Allow tracking yourself on all servers")
                    .setContexts(InteractionContextType.GUILD)
                    .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Разрешить отслеживать себя на всех серверах")
                    .setDescriptionLocalization(DiscordLocale.FRENCH, "Autoriser le suivi de soi sur tous les serveurs");

            commands.addCommands(
                            genderCommand,
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
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelay = 3600, initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    private void updateActivity() {
        if (!Config.isIsDev()) {
            try {
                int serverCount = jda.getGuilds().size();
                jda.getPresence().setActivity(Activity.playing(BotStartConfig.activity + serverCount + " guilds"));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void setLanguages() {
        try {
            List<String> listLanguages = new ArrayList<>();
            listLanguages.add("rus");
            listLanguages.add("eng");
            listLanguages.add("fre");

            for (String listLanguage : listLanguages) {
                InputStream inputStream = new ClassPathResource("json/" + listLanguage + ".json").getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                JSONObject jsonObject = new JSONObject(new JSONTokener(reader));

                for (String o : jsonObject.keySet()) {
                    switch (listLanguage) {
                        case "eng" -> ParserClass.english.put(o, String.valueOf(jsonObject.get(o)));
                        case "rus" -> ParserClass.russian.put(o, String.valueOf(jsonObject.get(o)));
                        case "fre" -> ParserClass.french.put(o, String.valueOf(jsonObject.get(o)));
                    }
                }
                reader.close();
                inputStream.close();
                reader.close();
            }
            System.out.println("setLanguages()");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void getGenders() {
        try {
            List<Gender> genderList = genderRepository.findAll();
            for (Gender gender : genderList) {
                mapGenders.put(gender.getUserId().toString(), gender.getGender());
            }
            System.out.println("getGenders()");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void getSuggestions() {
        try {
            List<Suggestions> suggestionsList = suggestionsRepository.findAll();
            NoticeRegistry instance = NoticeRegistry.getInstance();

            for (Suggestions suggestions : suggestionsList) {
                String suggestionUserId = String.valueOf(suggestions.getSuggestionUserId());
                String userId = String.valueOf(suggestions.getUserId());
                String guildId = String.valueOf(suggestions.getGuildId());

                Set<String> stringSet = instance.getAllUserTrackerIdsByUserId(guildId, userId);

                if (!stringSet.contains(suggestionUserId)) {
                    instance.addUserSuggestions(guildId, userId, suggestionUserId);
                }
            }
            System.out.println("getSuggestions()");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static Map<String, Language.LanguageEnum> getMapLanguages() {
        return mapLanguages;
    }

    public static Map<String, Lock.Locked> getMapLocks() {
        return mapLocks;
    }

    public static Map<String, Gender.GenderType> getMapGenders() {
        return mapGenders;
    }

    @Nullable
    public static Long getCommandId(String commandName) {
        return commandMap.get(commandName);
    }
}