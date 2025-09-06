package main.core.events;

import main.config.BotStartConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class JoinEvent {

    public void join(@NotNull GuildJoinEvent event) {
        try {
            Guild guild = event.getGuild();
            DefaultGuildChannelUnion defaultChannel = guild.getDefaultChannel();
            Member selfMember = guild.getSelfMember();
            if (defaultChannel == null) return;

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("NoticeMe");
            embedBuilder.addField("Help", "Use </help:1039918668135534624> to get information.", true);
            embedBuilder.addField("Language", "You can change Bot language: </language:1039918668135534623>", true);
            embedBuilder.addField("Notification Channel", "First you need to set up a channel for notifications: </setup:1039918668135534625>", true);
            embedBuilder.addField("Subscribe to a user", "Setting up receiving notifications: </sub:1040935591887519755>", true);

            MessageChannel messageChannel = defaultChannel.asStandardGuildMessageChannel();
            boolean hasPermission = selfMember.hasPermission(defaultChannel, Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_EMBED_LINKS);

            if (hasPermission) {
                messageChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }

            BotStartConfig.updateActivity();
        } catch (Exception ignored) {
        }
    }
}