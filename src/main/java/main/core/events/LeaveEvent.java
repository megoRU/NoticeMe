package main.core.events;

import main.config.BotStartConfig;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LeaveEvent {

    private final static Logger LOGGER = LoggerFactory.getLogger(LeaveEvent.class.getName());

    public void leave(@NotNull GuildLeaveEvent event) {
        LOGGER.info("GuildLeaveEvent: {}", event.getGuild().getId());

        BotStartConfig.updateActivity();
    }
}
