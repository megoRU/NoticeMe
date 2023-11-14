package main.core.events;

import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.repository.GuildRepository;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeleteButton {

    private static final ParserClass jsonParsers = new ParserClass();

    private final GuildRepository guildRepository;

    @Autowired
    public DeleteButton(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public void delete(@NotNull ButtonInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = event.getGuild().getIdLong();

        event.editButton(event.getButton().asDisabled()).queue();
        guildRepository.deleteById(guildIdLong);

        NoticeRegistry instance = NoticeRegistry.getInstance();
        instance.removeGuild(guildIdString);
        instance.removeServer(guildIdString);

        String deleteData = String.format(jsonParsers.getTranslation("delete_data", guildIdString));
        event.getHook().sendMessage(deleteData).setEphemeral(true).queue();
    }
}