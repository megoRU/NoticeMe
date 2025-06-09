package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.jsonparser.ParserClass;
import main.model.entity.Gender;
import main.model.repository.GenderRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GenderCommand {

    private final GenderRepository genderRepository;
    private static final ParserClass jsonParsers = new ParserClass();

    public void gender(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member eventMember = event.getMember();
        if (guild == null || eventMember == null) return;
        var guildIdString = guild.getId();
        var userId = event.getUser().getIdLong();
        String effectiveName = eventMember.getEffectiveName();
        String channelId = event.getChannel().getId();

        event.deferReply().setEphemeral(true).queue();

        String genderAsString = event.getOption("gender", OptionMapping::getAsString);
        if (genderAsString == null) return;
        Gender.GenderType genderType = Gender.GenderType.valueOf(genderAsString);
        String genderKey = (genderType == Gender.GenderType.FEMALE) ? "user_enter_female" : "user_enter_male";
        String genderText = jsonParsers.getTranslation(genderKey, guild.getId());

        Gender gender = new Gender();
        gender.setUserId(userId);
        gender.setGender(genderType);

        genderRepository.save(gender);

        BotStartConfig.getMapGenders().put(String.valueOf(userId), genderType);

        String genderSettings = String.format(jsonParsers.getTranslation("gender_settings", guildIdString), effectiveName, genderText, channelId);
        event.getHook().sendMessage(genderSettings).queue();
    }
}

