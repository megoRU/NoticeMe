package main.event;

import lombok.AllArgsConstructor;
import main.core.NoticeRegistry;
import main.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.repository.GuildRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
public class UserJoinEvent extends ListenerAdapter {

    private final GuildRepository guildRepository;
    private static final ParserClass jsonParsers = new ParserClass();

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (event.getChannelJoined() == null) return;

        Guild guild = event.getGuild();
        User user = event.getMember().getUser();
        VoiceChannel voiceChannel = event.getChannelJoined().asVoiceChannel();
        NoticeRegistry instance = NoticeRegistry.getInstance();

        ConcurrentMap<String, TrackingUser> trackingUserConcurrentMap = instance.getTrackingUserConcurrentMap().get(guild.getId());

        if (trackingUserConcurrentMap == null || trackingUserConcurrentMap.get(user.getId()) == null) return;
        String userList = trackingUserConcurrentMap.get(user.getId()).getUserList();
        Optional<main.model.entity.Guild> guildOptional = guildRepository.findById(guild.getIdLong());

        if (guildOptional.isPresent()) {
            main.model.entity.Guild guildDB = guildOptional.get();
            TextChannel textChannel = event.getGuild().getTextChannelById(guildDB.getTextChannelId());
            if (textChannel != null) {
                String text = String.format(jsonParsers.getTranslation("user_enter_to_channel", guild.getId()),
                        user.getId(),
                        voiceChannel.getId(),
                        userList);

                textChannel.sendMessage(text).queue();
            }
        }
    }
}