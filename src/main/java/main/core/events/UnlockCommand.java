package main.core.events;

import main.config.BotStartConfig;
import main.jsonparser.ParserClass;
import main.model.repository.LockRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UnlockCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final LockRepository lockRepository;

    @Autowired
    public UnlockCommand(LockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }

    public void unlock(@NotNull SlashCommandInteractionEvent event) {
        var user = event.getUser();
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();

        event.deferReply().setEphemeral(true).queue();
        BotStartConfig.getMapLocks().remove(user.getId());
        String unLockString = jsonParsers.getTranslation("unlock", guildIdString);
        lockRepository.deleteLockByUserId(user.getIdLong());

        event.getHook().sendMessage(unLockString).setEphemeral(true).queue();
    }
}
