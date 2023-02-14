package main.core.events;

import main.core.ChecksClass;
import main.jsonparser.ParserClass;
import main.model.entity.Server;
import main.model.repository.GuildRepository;
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

        if (guildChannelUnion.getType() == ChannelType.NEWS) {
            String canNotBeChannel = String.format(jsonParsers.getTranslation("can_not_be_channel", guildIdString), "NewsChannel");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.VOICE) {
            String canNotBeChannel = String.format(jsonParsers.getTranslation("can_not_be_channel", guildIdString), "VoiceChannel");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.STAGE) {
            String canNotBeChannel = String.format(jsonParsers.getTranslation("can_not_be_channel", guildIdString), "Stage");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.FORUM) {
            String canNotBeChannel = String.format(jsonParsers.getTranslation("can_not_be_channel", guildIdString), "Forum");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.GUILD_PUBLIC_THREAD
                || guildChannelUnion.getType() == ChannelType.GUILD_NEWS_THREAD
                || guildChannelUnion.getType() == ChannelType.GUILD_PRIVATE_THREAD) {
            String canNotBeChannel = String.format(jsonParsers.getTranslation("can_not_be_channel", guildIdString), "THREAD");
            event.getHook().sendMessage(canNotBeChannel).queue();
        } else if (guildChannelUnion.getType() == ChannelType.TEXT) {
            boolean bool = ChecksClass.canSend(guildChannelUnion, event);
            if (!bool) return;

            Server guild = new Server();
            guild.setGuildIdLong(guildId);
            guild.setTextChannelId(guildChannelUnion.asTextChannel().getIdLong());
            guildRepository.save(guild);

            String nowBotWillReceive = String.format(
                    jsonParsers.getTranslation("now_bot_will_receive", guildIdString),
                    guildChannelUnion.asTextChannel().getIdLong());
            event.getHook().sendMessage(nowBotWillReceive).setEphemeral(true).queue();
        }
    }
}
