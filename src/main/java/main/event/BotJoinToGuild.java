package main.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotJoinToGuild extends ListenerAdapter {

    //Bot join msg
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("NoticeMe");
            embedBuilder.addField("Help", "Use </help:1039918668135534624> to get information.", true);
            embedBuilder.addField("Language", "You can change Bot language: </language:1039918668135534623>", true);
            embedBuilder.addField("Notification Channel", "First you need to set up a channel for notifications: </setup:1039918668135534625>", true);
            embedBuilder.addField("Subscribe to a user", "Setting up receiving notifications: </sub:1040935591887519755>", true);

            DefaultGuildChannelUnion defaultChannel = event.getGuild().getDefaultChannel();
            if (defaultChannel != null) {
                TextChannel textChannel = defaultChannel.asTextChannel();
                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        } catch (Exception ignored) {
        }
    }
}