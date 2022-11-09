package main.slash;

import lombok.RequiredArgsConstructor;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class NoticeSlash extends ListenerAdapter {

    //REPO
    private final NoticeRepository noticeRepository;

    //LOGGER
    private final static Logger LOGGER = Logger.getLogger(NoticeSlash.class.getName());

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;

        LOGGER.info(String.format("\nSlash Command name: %s", event.getName()));





    }
}