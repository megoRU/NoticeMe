package main.core.events;

import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.repository.GuildRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SetupCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final GuildRepository guildRepository;

    @Autowired
    public SetupCommand(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public void setup(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildId = event.getGuild().getIdLong();

        event.deferReply().setEphemeral(true).queue();

        GuildChannelUnion guildChannelUnion = event.getOption("text-channel", OptionMapping::getAsChannel);

        if (guildChannelUnion == null) {
            event.getHook().sendMessage("text-channel is NULL").queue();
            return;
        }

        String canNotBeChannelTranslation = jsonParsers.getTranslation("can_not_be_channel", guildIdString);

        if (guildChannelUnion.getType() == ChannelType.NEWS) {
            String canNotBeChannel = String.format(canNotBeChannelTranslation, "NewsChannel");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.VOICE) {
            String canNotBeChannel = String.format(canNotBeChannelTranslation, "VoiceChannel");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.STAGE) {
            String canNotBeChannel = String.format(canNotBeChannelTranslation, "Stage");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.FORUM) {
            String canNotBeChannel = String.format(canNotBeChannelTranslation, "Forum");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.GUILD_PUBLIC_THREAD
                || guildChannelUnion.getType() == ChannelType.GUILD_NEWS_THREAD
                || guildChannelUnion.getType() == ChannelType.GUILD_PRIVATE_THREAD) {
            String canNotBeChannel = String.format(canNotBeChannelTranslation, "THREAD");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.TEXT) {
            Guild guild = event.getGuild();
            boolean hasPermission = guild.getSelfMember().hasPermission(guildChannelUnion, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);

            if (!hasPermission) {
                String noAccessSendMessage = jsonParsers.getTranslation("no_access_send_message", guildIdString);
                event.getHook().sendMessage(noAccessSendMessage).setEphemeral(true).queue();
                return;
            }

            Server server = new Server();
            server.setGuildIdLong(guildId);
            server.setTextChannelId(guildChannelUnion.asTextChannel().getIdLong());
            guildRepository.save(server);

            NoticeRegistry instance = NoticeRegistry.getInstance();
            instance.putServer(guildIdString, server);

            String nowBotWillReceive = jsonParsers.getTranslation("now_bot_will_receive", guildIdString);
            String format = String.format(nowBotWillReceive, guildChannelUnion.asTextChannel().getIdLong());

            event.getHook().sendMessage(format).setEphemeral(true).queue();
        }
    }
}
