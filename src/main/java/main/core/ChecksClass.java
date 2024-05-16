package main.core;

import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChecksClass {

    public static final ParserClass jsonParsers = new ParserClass();

    public static boolean canSend(GuildChannel dstChannel, Event event) {
        Member selfMember = dstChannel.getGuild().getSelfMember();
        StringBuilder stringBuilder = new StringBuilder();

        boolean bool = true;

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_SEND)) {
            stringBuilder.append("`Permission.MESSAGE_SEND`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.VIEW_CHANNEL)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.VIEW_CHANNEL`" : ",\n`Permission.VIEW_CHANNEL`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_HISTORY)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.MESSAGE_HISTORY`" : ",\n`Permission.MESSAGE_HISTORY`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_EMBED_LINKS)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.MESSAGE_EMBED_LINKS`" : ",\n`Permission.MESSAGE_EMBED_LINKS`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_ADD_REACTION)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.MESSAGE_ADD_REACTION`" : ",\n`Permission.MESSAGE_ADD_REACTION`");
            bool = false;
        }

        if (!bool && event != null && getGuild(event) != null) {
            String checkPermissions = String.format(
                    jsonParsers.getTranslation("check_permissions", Objects.requireNonNull(getGuild(event)).getId()),
                    dstChannel.getId(),
                    stringBuilder);

            sendMessage(event, checkPermissions);
            return false;
        }

        return bool;
    }

    @Nullable
    private static Guild getGuild(@NotNull Event event) {
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            return slashEvent.getGuild();
        } else {
            ButtonInteractionEvent buttonInteractionEvent = (ButtonInteractionEvent) event;
            return buttonInteractionEvent.getGuild();
        }
    }

    private static void sendMessage(@NotNull Event event, String checkPermissions) {
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            slashEvent.reply(checkPermissions).setEphemeral(true).queue();
        } else {
            ButtonInteractionEvent buttonInteractionEvent = (ButtonInteractionEvent) event;
            buttonInteractionEvent.reply(checkPermissions).setEphemeral(true).queue();
        }
    }
}