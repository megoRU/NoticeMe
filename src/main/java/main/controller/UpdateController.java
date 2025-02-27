package main.controller;

import lombok.Getter;
import main.core.CoreBot;
import main.core.NoticeMeUtils;
import main.core.events.*;
import main.model.repository.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Getter
@Component
public class UpdateController {

    //REPO
    private final NoticeRepository noticeRepository;
    private final GuildRepository guildRepository;
    private final LanguageRepository languageRepository;
    private final LockRepository lockRepository;
    private final EntriesRepository entriesRepository;
    private final SuggestionsRepository suggestionsRepository;

    //CORE
    private CoreBot coreBot;

    @Autowired
    public UpdateController(NoticeRepository noticeRepository,
                            GuildRepository guildRepository,
                            LanguageRepository languageRepository,
                            LockRepository lockRepository,
                            EntriesRepository entriesRepository,
                            SuggestionsRepository suggestionsRepository) {
        this.noticeRepository = noticeRepository;
        this.guildRepository = guildRepository;
        this.languageRepository = languageRepository;
        this.lockRepository = lockRepository;
        this.entriesRepository = entriesRepository;
        this.suggestionsRepository = suggestionsRepository;
    }

    public void registerBot(CoreBot coreBot) {
        this.coreBot = coreBot;
    }

    public void processEvent(Object event) {
        distributeEventsByType(event);
    }

    private void distributeEventsByType(Object event) {
        if (event instanceof SlashCommandInteractionEvent) {
            slashEvent((SlashCommandInteractionEvent) event);
        } else if (event instanceof GuildVoiceUpdateEvent) {
            userJoinChannelEvent((GuildVoiceUpdateEvent) event);
        } else if (event instanceof ButtonInteractionEvent) {
            buttonEvent((ButtonInteractionEvent) event);
        } else if (event instanceof GuildJoinEvent) {
            joinEvent((GuildJoinEvent) event);
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
            case "list" -> {
                ListCommand listCommand = new ListCommand();
                listCommand.list(event);
            }
            case "setup" -> {
                SetupCommand setupCommand = new SetupCommand(guildRepository);
                setupCommand.setup(event);
            }
            case "suggestion" -> {
                SuggestionCommand suggestionCommand = new SuggestionCommand(noticeRepository, suggestionsRepository);
                suggestionCommand.suggestion(event);
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
                unSubCommand.unsub_v2(event);
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

        String buttonId = event.getButton().getId();

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
            AddAllUsersButton addAllUsersButton = new AddAllUsersButton(guildRepository, noticeRepository);
            addAllUsersButton.addAllUsers(event);
        }
    }

    private void joinEvent(@NotNull GuildJoinEvent event) {
        JoinEvent joinEvent = new JoinEvent();
        joinEvent.join(event);
    }

    private void userJoinChannelEvent(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot()) return;
        if (event.getChannelJoined() == null) return;

        UserJoinEvent userJoinEvent = new UserJoinEvent(suggestionsRepository);
        userJoinEvent.userJoin(event);
    }
}