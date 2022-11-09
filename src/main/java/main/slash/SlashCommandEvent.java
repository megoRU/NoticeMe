package main.slash;

import lombok.RequiredArgsConstructor;
import main.model.entity.Guild;
import main.model.entity.Notice;
import main.model.repository.GuildRepository;
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

import java.util.Optional;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class SlashCommandEvent extends ListenerAdapter {

    //REPO
    private final NoticeRepository noticeRepository;
    private final GuildRepository guildRepository;

    //LOGGER
    private final static Logger LOGGER = Logger.getLogger(SlashCommandEvent.class.getName());

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;
        if (event.getGuild() == null) return;

        User user = event.getUser();
        long guildId = event.getGuild().getIdLong();

        LOGGER.info(String.format("\nSlash Command name: %s", event.getName()));

        if (event.getName().equals("setup")) {
            GuildChannelUnion guildChannelUnion = event.getOption("text-channel", OptionMapping::getAsChannel);
            if (guildChannelUnion instanceof TextChannel) {
                Guild guild = new Guild();
                guild.setGuildId(guildId);
                guild.setTextChannelId(guildChannelUnion.asTextChannel().getIdLong()); //NPE
                guildRepository.save(guild);

                String text =
                        "Теперь уведомления будут приходить в канал: <#" + guildChannelUnion.asTextChannel().getIdLong() + ">\n" +
                                "Не забудьте сделать его общедоступным, и чтобы бот имел доступ к нему.";
                event.reply(text).queue();
            } else if (guildChannelUnion instanceof NewsChannel) {
                event.reply("это не может быть канал NewsChannel").queue();
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
                event.reply("Вы не можете выбрать себя").queue();
                return;
            } else if (user.isBot()) {
                event.reply("Вы не можете выбрать бота").queue();
                return;
            }

            Optional<Guild> guildOptional = guildRepository.findById(guildId);
            if (guildOptional.isPresent()) {
                Notice notice = new Notice();
                notice.setGuildId(guildOptional.get());
                notice.setUserId(user.getIdLong());
                notice.setUserTrackingId(userDest.getIdLong());
                noticeRepository.save(notice);

                event.reply("Теперь вы будите получать уведомление, когда пользователь: <@" + userDest.getIdLong() +
                        ">\nбудет заходить в голосовой канал").queue();
            } else {
                event.reply("Гильдия ещё не настроена. Используйте `/setup`").queue();
            }
            return;
        }



    }
}