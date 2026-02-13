package main.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.core.NoticeMeUtils;
import main.core.events.*;
import main.model.repository.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Getter
@AllArgsConstructor
@Slf4j
@Component
public class UpdateController {

    //REPO
    private final NoticeRepository noticeRepository;
    private final GuildRepository guildRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;
    private final GenderRepository genderRepository;

    public void processEvent(Object event) {
        distributeEventsByType(event);
    }

    private void distributeEventsByType(Object event) {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
            log.info(slashCommandInteractionEvent.getName());
            slashEvent(slashCommandInteractionEvent);
        } else if (event instanceof GuildVoiceUpdateEvent guildVoiceUpdateEvent) {
            userJoinChannelEvent(guildVoiceUpdateEvent);
        } else if (event instanceof ButtonInteractionEvent buttonInteractionEvent) {
            log.info(buttonInteractionEvent.getButton().getLabel());
            buttonEvent(buttonInteractionEvent);
        } else if (event instanceof GuildJoinEvent guildJoinEvent) {
            joinEvent(guildJoinEvent);
        } else if (event instanceof GuildLeaveEvent guildLeaveEvent) {
            leaveEvent(guildLeaveEvent);
        }
    }

    private void slashEvent(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;

        switch (event.getName()) {
            case "help" -> {
                HelpCommand helpCommand = new HelpCommand();
                helpCommand.help(event);
            }
            case "language" -> {
                LanguageCommand languageCommand = new LanguageCommand(languageRepository);
                languageCommand.language(event);
            }
            case "lock" -> {
                LockCommand lockCommand = new LockCommand(lockRepository, noticeRepository);
                lockCommand.lock(event);
            }
            case "unlock" -> {
                UnlockCommand unlockCommand = new UnlockCommand(lockRepository);
                unlockCommand.unlock(event);
            }
            case "donate" -> {
                DonateCommand donateCommand = new DonateCommand();
                donateCommand.donate(event);
            }
            case "delete" -> {
                DeleteCommand deleteCommand = new DeleteCommand();
                deleteCommand.delete(event);
            }
            case "gender" -> {
                GenderCommand genderCommand = new GenderCommand(genderRepository);
                genderCommand.gender(event);
            }
            case "list" -> {
                ListCommand listCommand = new ListCommand();
                listCommand.list(event);
            }
            case "setup" -> {
                SetupCommand setupCommand = new SetupCommand(guildRepository);
                setupCommand.setup(event);
            }
            case "sub" -> {
                SubCommand subCommand = new SubCommand(noticeRepository);
                subCommand.sub(event);
            }
            case "unsub" -> {
                UnSubCommand unSubCommand = new UnSubCommand(noticeRepository);
                unSubCommand.unsub(event);
            }
            case "unsub-v2" -> {
                UnSubCommand unSubCommand = new UnSubCommand(noticeRepository);
                unSubCommand.unsubV2(event);
            }
            case "check" -> {
                Check check = new Check();
                check.permission(event);
            }
        }
    }

    private void buttonEvent(@NotNull ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;
        if (event.getGuild() == null) return;

        String buttonId = event.getButton().getCustomId();

        if (Objects.equals(buttonId, NoticeMeUtils.BUTTON_DELETE)) {
            DeleteButton deleteButton = new DeleteButton(guildRepository);
            deleteButton.delete(event);
            return;
        }

        if (buttonId != null && buttonId.contains(NoticeMeUtils.BUTTON_ADD_USER)) {
            AddUserButton addUserButton = new AddUserButton(noticeRepository);
            addUserButton.addUser(event);
            return;
        }

        if (Objects.equals(buttonId, NoticeMeUtils.BUTTON_ALL_USERS)) {
            AddAllUsersButton addAllUsersButton = new AddAllUsersButton(noticeRepository);
            addAllUsersButton.addAllUsers(event);
        }
    }

    private void joinEvent(@NotNull GuildJoinEvent event) {
        JoinEvent joinEvent = new JoinEvent();
        joinEvent.join(event);
    }

    private void leaveEvent(@NotNull GuildLeaveEvent event) {
        LeaveEvent leaveEvent = new LeaveEvent();
        leaveEvent.leave(event);
    }

    private void userJoinChannelEvent(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (event.getChannelJoined() == null) return;

        UserJoinEvent userJoinEvent = new UserJoinEvent();
        userJoinEvent.userJoin(event);
    }
}