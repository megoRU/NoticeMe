package main.core.events;

import main.config.BotStartConfig;
import main.jsonparser.ParserClass;
import main.model.entity.Language;
import main.model.repository.LanguageRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LanguageCommand {

    private final LanguageRepository languageRepository;

    private static final ParserClass jsonParsers = new ParserClass();

    @Autowired
    public LanguageCommand(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    public void language(@NotNull SlashCommandInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildId = event.getGuild().getIdLong();

        event.deferReply().setEphemeral(true).queue();

        String languageAsString = event.getOption("bot", OptionMapping::getAsString);
        if (languageAsString == null) return;

        Language.LanguageEnum languageEnum = Language.LanguageEnum.valueOf(languageAsString);

        Language language = new Language();
        language.setGuildId(guildId);
        language.setLanguage(languageEnum);

        languageRepository.save(language);

        BotStartConfig.getMapLanguages().put(guildIdString, languageEnum);
        String languageSet = jsonParsers.getTranslation("language_set", guildIdString);
        event.getHook().sendMessage(languageSet).queue();
    }
}