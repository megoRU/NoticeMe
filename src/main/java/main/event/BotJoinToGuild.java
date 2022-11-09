package main.event;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotJoinToGuild extends ListenerAdapter {

    //Bot join msg
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        try {
            event.getGuild()
                    .getDefaultChannel()
                    .asTextChannel()
                    .sendMessage(
                            "Thanks for adding " + "**NoticeMe**" + " to " + event.getGuild().getName() + "!"
                                    + "\nUse </help:1039918668135534624> to get information."
                                    + "\nYou can change Bot language: </language:1039918668135534623>"
                                    + "\nFirst you need to set up a channel for notifications: </setup:1039918668135534625>"
                                    + "\nSetting up receiving notifications: </notice:1039918668571750420>").queue();
        } catch (Exception ignored) {
        }
    }
}