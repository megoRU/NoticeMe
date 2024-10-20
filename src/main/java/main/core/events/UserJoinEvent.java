package main.core.events;

import jakarta.annotation.Nullable;
import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.entity.Entries;
import main.model.entity.Server;
import main.model.repository.EntriesRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserJoinEvent {

    private static final ParserClass jsonParsers = new ParserClass();
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static Logger LOGGER = Logger.getLogger(UserJoinEvent.class.getName());

    private final EntriesRepository entriesRepository;

    @Autowired
    public UserJoinEvent(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
    }

    public void userJoin(@NotNull GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        User user = event.getMember().getUser();
        Member selfMember = guild.getSelfMember();

        try {
            VoiceChannel voiceChannel = getAsChannel(event.getChannelJoined());
            if (voiceChannel == null) return;

            String name = voiceChannel.getName();
            boolean hasPermissionViewChannel = selfMember.hasPermission(voiceChannel, Permission.VIEW_CHANNEL);

            if (!hasPermissionViewChannel || name.contains("AFK")) return;

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            List<Member> members = voiceChannel.getMembers(); //Always 1+ users

            if (members.size() > 1) {
                //Это должно быть тут
                Entries entries = new Entries();
                entries.setGuildId(guild.getIdLong());
                entries.setChannelId(voiceChannel.getIdLong());
                entries.setUserId(user.getIdLong());
                entries.setUsersInChannel(members);
                entries.setJoinTime(Timestamp.valueOf(simpleDateFormat.format(timestamp)));
                CompletableFuture.runAsync(() -> entriesRepository.save(entries));
            }

            NoticeRegistry instance = NoticeRegistry.getInstance();
            TrackingUser instanceUser = instance.getUser(guild.getId(), user.getId());

            if (instanceUser == null) return;
            String userList = instanceUser.getUserList();
            if (!instanceUser.hasUserJoin()) {
                Server server = instance.getServer(guild.getId());
                if (server != null) {
                    TextChannel textChannel = guild.getTextChannelById(server.getTextChannelId());
                    if (textChannel != null) {
                        if (selfMember.hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                            String text = String.format(jsonParsers.getTranslation("user_enter_to_channel", guild.getId()),
                                    user.getName(),
                                    voiceChannel.getId(),
                                    userList);

                            textChannel.sendMessage(text).queue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "UserJoinEvent: ", e);
        }
    }

    @Nullable
    private VoiceChannel getAsChannel(AudioChannelUnion audioChannelUnion) {
        if (audioChannelUnion instanceof VoiceChannel) return audioChannelUnion.asVoiceChannel();
        else {
            LOGGER.info(audioChannelUnion.getName() + " is not a VoiceChannel!");
            return null;
        }
    }
}