package main.core.events;

import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class HelpCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    public void help(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();

        EmbedBuilder info = new EmbedBuilder();
        info.setColor(Color.GREEN);
        info.setTitle("NoticeMe");

        String language = jsonParsers.getTranslation("language", guildIdString);
        String setup = jsonParsers.getTranslation("setup", guildIdString);
        String list = jsonParsers.getTranslation("list", guildIdString);
        String unsub = jsonParsers.getTranslation("unsub", guildIdString);
        String sub = jsonParsers.getTranslation("sub", guildIdString);
        String delete = jsonParsers.getTranslation("delete", guildIdString);
        String suggestion = jsonParsers.getTranslation("suggestion", guildIdString);
        String helpLock = jsonParsers.getTranslation("help_lock", guildIdString);
        String helpUnlock = jsonParsers.getTranslation("help_unlock", guildIdString);
        String helpPs = jsonParsers.getTranslation("help_ps", guildIdString);
        String check = jsonParsers.getTranslation("check", guildIdString);

        String text = String.format(
                """
                        </language:1039918668135534623> - %s
                        </setup:1039918668135534625> - %s
                        </list:1040218561261613157> - %s
                        </unsub:1040218561261613158> - %s
                        </sub:1040935591887519755> - %s
                        </delete:1041093816620429385> - %s
                        </suggestion:1045316663718969357> - %s
                        </lock:1045675151473254470> - %s
                        </unlock:1045675151473254471> - %s
                        </check:1241705257822847012> - %s
                        
                        P. S.:
                        %s
                        """, language, setup, list, unsub, sub, delete, suggestion, helpLock, helpUnlock, check, helpPs);

        String slashCommands = jsonParsers.getTranslation("slash_commands", guildIdString);
        String messagesEventsLinks = jsonParsers.getTranslation("messages_events_links", guildIdString);
        String messagesEventsSite = jsonParsers.getTranslation("messages_events_site", guildIdString);
        String messagesEventsAddMeToOtherGuilds = jsonParsers.getTranslation("messages_events_add_me_to_other_guilds", guildIdString);

        info.addField(slashCommands, text, false);
        info.addField(messagesEventsLinks, messagesEventsSite + messagesEventsAddMeToOtherGuilds, false);

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.link("https://discord.gg/UrWG3R683d", "Support"));

        event.replyEmbeds(info.build()).setEphemeral(true).addActionRow(buttons).queue();
    }
}