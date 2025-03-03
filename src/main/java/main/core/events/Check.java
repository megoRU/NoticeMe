package main.core.events;

import lombok.AllArgsConstructor;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
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

    private static final ParserClass jsonParsers = new ParserClass();
    private static final NoticeRegistry instance = NoticeRegistry.getInstance();

    //TODO: Надо проверить когда отсутствует канал, или когда нет настройки
    public void permission(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        event.deferReply().setEphemeral(true).queue();

        String guildIdString = guild.getId();
        Member selfMember = guild.getSelfMember();

        Server server = instance.getServer(guildIdString);
        String youCannotSetChannel = jsonParsers.getTranslation("you_cannot_set_channel", guildIdString);

        if (server == null) {
            event.getHook().sendMessage(youCannotSetChannel).setEphemeral(true).queue();
            return;
        }

        String channelNotification = jsonParsers.getTranslation("channel_notification", guildIdString);
        String acceptToVoiceChannel = jsonParsers.getTranslation("accept_to_voice_channel", guildIdString);

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
            boolean hasPermission = selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT);
            String voiceChannelName = voiceChannel.getName();
            stringBuilder.append("<#").append(voiceChannel.getId()).append(">").append(": ");
            if (hasPermission && !voiceChannelName.contains("AFK")) {
                stringBuilder.append("✅");
            } else {
                stringBuilder.append("❌");
            }
            stringBuilder.append("\n");
        });

        event.getHook().sendMessage(stringBuilder.toString()).queue();
    }
}