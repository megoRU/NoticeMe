package main.event;

import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ChecksClass {

    public static final ParserClass jsonParsers = new ParserClass();

    public static boolean canSend(GuildChannel dstChannel, SlashCommandInteractionEvent event) {
        Member selfMember = dstChannel.getGuild().getSelfMember();
        StringBuilder stringBuilder = new StringBuilder();

        boolean bool = true;

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_SEND)) {
            stringBuilder.append("`Permission.MESSAGE_SEND`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.VIEW_CHANNEL)) {
            stringBuilder.append(stringBuilder.length() == 0 ? "`Permission.VIEW_CHANNEL`" : ", `Permission.VIEW_CHANNEL`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_HISTORY)) {
            stringBuilder.append(stringBuilder.length() == 0 ? "`Permission.MESSAGE_HISTORY`" : ", `Permission.MESSAGE_HISTORY`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_EMBED_LINKS)) {
            stringBuilder.append(stringBuilder.length() == 0 ? "`Permission.MESSAGE_EMBED_LINKS`" : ", `Permission.MESSAGE_EMBED_LINKS`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_ADD_REACTION)) {
            stringBuilder.append(stringBuilder.length() == 0 ? "`Permission.MESSAGE_ADD_REACTION`" : ", `Permission.MESSAGE_ADD_REACTION`");
            bool = false;
        }

        if (!bool && event != null && event.getGuild() != null) {
            String checkPermissions = String.format(
                    jsonParsers.getTranslation("check_permissions", event.getGuild().getId()),
                    dstChannel.getId(),
                    stringBuilder);

            event.getHook().sendMessage(checkPermissions).setEphemeral(true).queue();
            return false;
        }

        return bool;
    }
}
