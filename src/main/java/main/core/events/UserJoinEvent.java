package main.core.events;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import main.jsonparser.ParserClass;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class UserJoinEvent {

    private static final ParserClass jsonParsers = new ParserClass();
    private final static Logger LOGGER = Logger.getLogger(UserJoinEvent.class.getName());
    private final static NoticeRegistry noticeRegistry = NoticeRegistry.getInstance();

    private final SuggestionsRepository suggestionsRepository;

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

            List<Member> members = voiceChannel.getMembers(); //Always 1+ users

            if (members.size() > 1) {
                CompletableFuture.runAsync(() -> updateUserSuggestions(user.getId(), members, guild.getIdLong()));
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

    private void updateUserSuggestions(String userId, List<Member> members, long guildId) {
        Set<String> currentSuggestions = noticeRegistry.getSuggestions(userId);

        if (currentSuggestions == null) {
            List<Suggestions> dbSuggestions = suggestionsRepository.findAllByUserId(Long.parseLong(userId));
            addBotSuggestions(userId, members);

            if (dbSuggestions.isEmpty()) {
                noticeRegistry.getSuggestions(userId).forEach(suggestion -> saveSuggestion(userId, suggestion, guildId));
            } else {
                List<Long> dbSuggestionIds = dbSuggestions.stream()
                        .map(Suggestions::getSuggestionUserId)
                        .toList();

                members.stream()
                        .filter(member -> !dbSuggestionIds.contains(member.getUser().getIdLong()))
                        .filter(member -> !member.getUser().isBot())
                        .forEach(member -> saveSuggestion(userId, member.getUser().getId(), guildId));
            }
        } else {
            members.stream()
                    .filter(member -> !currentSuggestions.contains(member.getUser().getId()))
                    .filter(member -> !member.getUser().isBot())
                    .forEach(member -> {
                        noticeRegistry.addUserSuggestions(userId, member.getUser().getId());
                        saveSuggestion(userId, member.getUser().getId(), guildId);
                    });
        }
    }

    private void addBotSuggestions(String userId, List<Member> members) {
        members.stream()
                .filter(member -> member.getUser().isBot())
                .forEach(member -> noticeRegistry.addUserSuggestions(userId, member.getUser().getId()));
    }

    private void saveSuggestion(String userId, String suggestionUserId, long guildId) {
        Suggestions suggestion = new Suggestions();
        suggestion.setUserId(Long.parseLong(userId));
        suggestion.setGuildId(guildId);
        suggestion.setSuggestionUserId(Long.parseLong(suggestionUserId));
        suggestionsRepository.save(suggestion);
    }
}