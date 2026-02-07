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
import main.model.entity.Suggestions;
import main.model.repository.SuggestionsRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Slf4j
@AllArgsConstructor
public class UserJoinEvent {

    private static final ParserClass jsonParsers = new ParserClass();
    private final static Logger LOGGER = Logger.getLogger(UserJoinEvent.class.getName());
    private final static NoticeRegistry instance = NoticeRegistry.getInstance();

    private final SuggestionsRepository suggestionsRepository;

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

            List<Member> members = voiceChannel.getMembers().stream()
                    .filter(member -> !member.getUser().isBot())
                    .filter(member -> !member.getUser().getId().equals(userId))
                    .toList();

            TrackingUser instanceUser = instance.getUser(guild.getIdLong(), userIdLong);

            if (!members.isEmpty()) {
                CompletableFuture.runAsync(() -> updateUserSuggestions(userIdLong, members, guild.getIdLong(), instanceUser));
            }

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

    // Сохраняет предложения, но только тех которых нет в БД
    // userId кто зашел
    // instanceUser кто, зашел и его список кто на него подписан
    private void updateUserSuggestions(Long userId, List<Member> members, long guildId, @Nullable TrackingUser instanceUser) {
        Set<Long> stringSet = instance.getUserTrackerIdsByUserId(guildId, userId);

        Set<Long> currentSuggestions = instance.getSuggestionsList(guildId, userId);
        Set<Long> subscribedUsers = (instanceUser != null) ? instanceUser.getUserListSet() : Collections.emptySet();

        List<Member> newSuggestions = members.stream()
                .filter(member -> !currentSuggestions.contains(member.getUser().getIdLong()))
                .filter(member -> !subscribedUsers.contains(member.getUser().getIdLong()))
                .filter(member -> !stringSet.contains(member.getUser().getIdLong()))
                .toList();

        if (!newSuggestions.isEmpty()) {
            newSuggestions.forEach(member -> {
                saveSuggestion(userId, member, guildId);
                addSuggestions(userId, member, guildId);
            });
        }
    }

    private void addSuggestions(Long userId, Member suggestionMember, Long guildId) {
        Long suggestionUserId = suggestionMember.getUser().getIdLong();
        instance.addUserSuggestions(guildId, userId, suggestionUserId);
    }

    private void saveSuggestion(Long userId, Member suggestionMember, long guildId) {
        long suggestionUserId = suggestionMember.getUser().getIdLong();

        Suggestions suggestion = new Suggestions();
        suggestion.setUserId(userId);
        suggestion.setGuildId(guildId);
        suggestion.setSuggestionUserId(suggestionUserId);

        suggestionsRepository.save(suggestion);
    }
}