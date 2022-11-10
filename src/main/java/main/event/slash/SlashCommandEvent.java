package main.event.slash;

import lombok.RequiredArgsConstructor;
import main.config.BotStartConfig;
import main.jsonparser.ParserClass;
import main.model.entity.Guild;
import main.model.entity.Language;
import main.model.entity.Notice;
import main.model.repository.GuildRepository;
import main.model.repository.LanguageRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class SlashCommandEvent extends ListenerAdapter {

    //REPO
    private final NoticeRepository noticeRepository;
    private final GuildRepository guildRepository;
    private final LanguageRepository languageRepository;

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
                Guild guild = new Guild();
                guild.setGuildId(guildId);
                guild.setTextChannelId(guildChannelUnion.asTextChannel().getIdLong()); //NPE
                guildRepository.save(guild);

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

        if (event.getName().equals("notice")) {
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

            Optional<Guild> guildOptional = guildRepository.findById(guildId);
            if (guildOptional.isPresent()) {
                Notice notice = new Notice();
                notice.setGuildId(guildOptional.get());
                notice.setUserId(user.getIdLong());
                notice.setUserTrackingId(userDest.getIdLong());
                noticeRepository.save(notice);

                String nowYouWillReceive = String.format(jsonParsers.getTranslation("now_you_will_receive", guildIdString), userDest.getIdLong());
                event.reply(nowYouWillReceive).queue();
            } else {
                String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);
                event.reply(youCannotSetChannel).queue();
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

        if (event.getName().equals("list")) {
            List<Notice> noticeList = noticeRepository.findAllByUserIdAndGuildId(user.getIdLong(), guildId);
            if (!noticeList.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();

                for (Notice notice : noticeList) {
                    if (stringBuilder.isEmpty()) {
                        stringBuilder.append("<@").append(notice.getUserTrackingId()).append(">");
                    } else {
                        stringBuilder.append(", <@").append(notice.getUserTrackingId()).append(">");
                    }
                }
                String subscription = String.format(jsonParsers.getTranslation("subscription", guildIdString), stringBuilder);
                event.reply(subscription).queue();
            } else {
                String emptyList = jsonParsers.getTranslation("empty_list", guildIdString);
                event.reply(emptyList).queue();
            }
            return;
        }

        if (event.getName().equals("unsub")) {
            User userFromOptions = event.getOption("user", OptionMapping::getAsUser);
            if (userFromOptions == null) return;
            Notice notice = noticeRepository.findTrackingUser(user.getIdLong(), guildId, userFromOptions.getIdLong());

            if (notice == null) {
                String dontFindUser = jsonParsers.getTranslation("dont_find_user", guildIdString);
                event.reply(dontFindUser).queue();
            } else {
                noticeRepository.deleteByUserTrackingId(notice.getUserTrackingId());
                String successfullyDeleted = String.format(jsonParsers.getTranslation("successfully_deleted", guildIdString), notice.getUserTrackingId());
                event.reply(successfullyDeleted).queue();
            }
            return;
        }





    }
}