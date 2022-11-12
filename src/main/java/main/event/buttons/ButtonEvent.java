package main.event.buttons;

import lombok.AllArgsConstructor;
import main.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.repository.GuildRepository;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AllArgsConstructor
public class ButtonEvent extends ListenerAdapter {

    public static final String BUTTON_DELETE = "BUTTON_DELETE";
    private static final ParserClass jsonParsers = new ParserClass();
    private final GuildRepository guildRepository;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;
        if (event.getGuild() == null) return;
        long GuildIdLong = event.getGuild().getIdLong();
        String guildIdString = event.getGuild().getId();

        if (Objects.equals(event.getButton().getId(), BUTTON_DELETE)) {
            event.editButton(event.getButton().asDisabled()).queue();
            guildRepository.deleteById(GuildIdLong);
            NoticeRegistry.getInstance().removeGuild(guildIdString);
            String deleteData = String.format(jsonParsers.getTranslation("delete_data", guildIdString));
            event.getHook().sendMessage(deleteData).setEphemeral(true).queue();
        }
    }

}
