package main.event;

import lombok.AllArgsConstructor;
import main.core.NoticeRegistry;
import main.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.entity.Entries;
import main.model.entity.Server;
import main.model.repository.EntriesRepository;
import main.model.repository.GuildRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class UserJoinEvent extends ListenerAdapter {

    private static final ParserClass jsonParsers = new ParserClass();
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final GuildRepository guildRepository;
    private final EntriesRepository entriesRepository;

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (event.getChannelJoined() == null) return;

        Guild guild = event.getGuild();
        User user = event.getMember().getUser();

        try {
            VoiceChannel voiceChannel = getAsChannel(event.getChannelJoined());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            List<Member> members = voiceChannel.getMembers(); //Always 1+ users

            Entries entries = new Entries();
            entries.setGuildId(guild.getIdLong());
            entries.setChannelId(voiceChannel.getIdLong());
            entries.setUserId(user.getIdLong());
            entries.setUsersInChannel(members);
            entries.setJoinTime(Timestamp.valueOf(simpleDateFormat.format(timestamp)));
            entriesRepository.save(entries);

            NoticeRegistry instance = NoticeRegistry.getInstance();
            TrackingUser instanceUser = instance.getUser(guild.getId(), user.getId());

            if (instanceUser == null) return;
            String userList = instanceUser.getUserList();
            Optional<Server> guildOptional = guildRepository.findById(guild.getIdLong());

            if (guildOptional.isPresent()) {
                if (!instanceUser.hasUserJoin()) {
                    Server guildDB = guildOptional.get();
                    TextChannel textChannel = event.getGuild().getTextChannelById(guildDB.getTextChannelId());
                    if (textChannel != null) {
                        if (event.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
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
            e.printStackTrace();
            System.out.println("UserJoinEvent: " + e.getMessage());
        }
    }

    private VoiceChannel getAsChannel(AudioChannelUnion audioChannelUnion) {
        if (audioChannelUnion instanceof VoiceChannel) return audioChannelUnion.asVoiceChannel();
        else throw new IllegalArgumentException(audioChannelUnion.getName() + " is not a VoiceChannel!");
    }
}