package main.core.events;

import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Objects;

@Service
public class DonateCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    public void donate(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();

        String donate = jsonParsers.getTranslation("donate", guildIdString);

        Button tinkoff = Button.link("https://www.tinkoff.ru/rm/savin.yuriy8/DyrGO46875", "Tinkoff");

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Donate");
        embedBuilder.setColor(Color.YELLOW);
        embedBuilder.appendDescription(donate);
        event.replyEmbeds(embedBuilder.build()).addActionRow(tinkoff).queue();
    }
}