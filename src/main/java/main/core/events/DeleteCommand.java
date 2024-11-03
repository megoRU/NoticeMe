package main.core.events;

import main.core.NoticeMeUtils;
import main.jsonparser.ParserClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeleteCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    public void delete(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();

        String warningDeleteData = jsonParsers.getTranslation("warning_delete_data", guildIdString);
        event.reply(warningDeleteData)
                .setEphemeral(true)
                .setActionRow(Button.danger(NoticeMeUtils.BUTTON_DELETE, "Delete"))
                .queue();
    }
}