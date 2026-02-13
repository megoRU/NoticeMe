package main.core.events;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
import main.model.entity.Gender;
import main.model.entity.Server;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class UserJoinEvent {

    private static final ParserClass jsonParsers = new ParserClass();
    private final static NoticeRegistry instance = NoticeRegistry.getInstance();

    public void userJoin(@NotNull GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member eventMember = event.getMember();
        User user = eventMember.getUser();
        String userId = user.getId();
        Long userIdLong = user.getIdLong();

        Member selfMember = guild.getSelfMember();
        String effectiveName = eventMember.getEffectiveName();

        try {
            VoiceChannel voiceChannel = getAsChannel(event.getChannelJoined());
            if (voiceChannel == null) return;

            String name = voiceChannel.getName();
            boolean hasPermissionViewChannel = selfMember.hasPermission(voiceChannel, Permission.VIEW_CHANNEL);

            if (!hasPermissionViewChannel || name.contains("AFK")) return;

            TrackingUser instanceUser = instance.getUser(guild.getIdLong(), userIdLong);

            if (instanceUser == null) return;
            String userList = instanceUser.getUserList();

            if (!instanceUser.hasUserJoin()) {
                Server server = instance.getServer(guild.getIdLong());

                if (server != null) {
                    TextChannel textChannel = guild.getTextChannelById(server.getTextChannelId());

                    if (textChannel != null) {
                        if (selfMember.hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                            Map<String, Gender.GenderType> mapGenders = BotStartConfig.getMapGenders();
                            Gender.GenderType genderType = mapGenders.get(userId);
                            if (genderType == null) genderType = Gender.GenderType.MALE;

                            String genderKey = (genderType == Gender.GenderType.FEMALE) ? "user_enter_female" : "user_enter_male";
                            String genderText = jsonParsers.getTranslation(genderKey, guild.getId());
                            String template = jsonParsers.getTranslation("user_enter_to_channel", guild.getId());

                            log.info("user_enter_to_channel template='{}', effectiveName='{}', genderText='{}', name='{}', userList='{}'", template, effectiveName, genderText, name, userList);

                            try {
                                String text = String.format(template, effectiveName, genderText, name, userList);
                                log.info("Formatted message='{}'", text);
                                textChannel.sendMessage(text).queue();
                            } catch (Exception e) {
                                log.error("Formatting error. template='{}'", template, e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("UserJoinEvent: ", e);
        }
    }

    @Nullable
    private VoiceChannel getAsChannel(AudioChannelUnion audioChannelUnion) {
        if (audioChannelUnion instanceof VoiceChannel) return audioChannelUnion.asVoiceChannel();
        else {
            log.info("{} is not a VoiceChannel!", audioChannelUnion.getName());
            return null;
        }
    }
}