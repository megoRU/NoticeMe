package main.core.events;

import lombok.AllArgsConstructor;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.repository.GuildRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class Check {

    private final GuildRepository guildRepository;
    private static final ParserClass jsonParsers = new ParserClass();

    public void permission(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        event.deferReply().setEphemeral(true).queue();

        Long guildId = guild.getIdLong();
        Member selfMember = guild.getSelfMember();
        Server server = guildRepository.findServerByGuildIdLong(guildId);
        String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildId.toString());

        if (server == null) {
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
            return;
        }

        String channelNotification = jsonParsers.getTranslation("channel_notification", guildId.toString());
        String acceptToVoiceChannel = jsonParsers.getTranslation("accept_to_voice_channel", guildId.toString());

        Long textChannelId = server.getTextChannelId();
        TextChannel textChannel = guild.getTextChannelById(textChannelId);

        if (textChannel == null) {
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (!selfMember.hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
            String format = String.format(channelNotification, textChannel.getId(), "❌");
            stringBuilder.append(format);
        } else {
            String format = String.format(channelNotification, textChannel.getId(), "✅");
            stringBuilder.append(format);
        }

        stringBuilder.append("\n\n");
        stringBuilder.append(acceptToVoiceChannel);
        stringBuilder.append("\n");

        List<VoiceChannel> voiceChannelList = guild.getVoiceChannels();

        voiceChannelList.forEach(voiceChannel -> {
            boolean hasPermission = selfMember.hasPermission(voiceChannel, Permission.VIEW_CHANNEL);
            stringBuilder.append("<#").append(voiceChannel.getId()).append(">").append(": ");
            if (hasPermission) {
                stringBuilder.append("✅");
            } else {
                stringBuilder.append("❌");
            }
            stringBuilder.append("\n");
        });

        event.getHook().sendMessage(stringBuilder.toString()).queue();
    }
}