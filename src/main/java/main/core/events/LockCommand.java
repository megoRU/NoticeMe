package main.core.events;

import main.config.BotStartConfig;
import main.core.core.NoticeRegistry;
import main.jsonparser.ParserClass;
import main.model.entity.Lock;
import main.model.repository.LockRepository;
import main.model.repository.NoticeRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LockCommand {

    private static final ParserClass jsonParsers = new ParserClass();

    private final LockRepository lockRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    public LockCommand(LockRepository lockRepository, NoticeRepository noticeRepository) {
        this.lockRepository = lockRepository;
        this.noticeRepository = noticeRepository;
    }

    public void lock(@NotNull SlashCommandInteractionEvent event) {
        var user = event.getUser();
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();

        event.deferReply().setEphemeral(true).queue();

        BotStartConfig.getMapLocks().put(user.getId(), Lock.Locked.LOCKED);
        String lockString = jsonParsers.getTranslation("lock", guildIdString);

        NoticeRegistry instance = NoticeRegistry.getInstance();
        instance.removeUserFromAllGuild(user.getIdLong());
        noticeRepository.deleteAllByUserTrackingId(user.getId());

        Lock lock = new Lock();
        lock.setUserId(user.getIdLong());
        lock.setLocked(Lock.Locked.LOCKED);
        lockRepository.save(lock);

        event.getHook().sendMessage(lockString).setEphemeral(true).queue();
    }
}