package main.core.events;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.core.NoticeMeUtils;
import main.core.core.NoticeRegistry;
import main.model.entity.Advertisement;
import main.model.repository.AdvertisementRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
@Service
public class DisableAds {

    private final AdvertisementRepository advertisementRepository;

    public void disable(@NotNull ButtonInteractionEvent event) {
        var guildIdString = Objects.requireNonNull(event.getGuild()).getId();
        var guildIdLong = event.getGuild().getIdLong();
        Member member = event.getMember();
        GuildMessageChannelUnion guildChannel = event.getGuildChannel();
        if (member == null) return;

        boolean hasPermission = member.hasPermission(guildChannel, Permission.ADMINISTRATOR);

        if (!hasPermission) {
            event.reply("Только администраторы могут отключить рекламу!").setEphemeral(true).queue();
            return;
        }

        event.editButton(event.getButton().asDisabled()).queue();

        Advertisement advertisement = new Advertisement();
        advertisement.setGuildId(guildIdLong);
        advertisement.setStatus(Advertisement.Status.DISABLED);
        advertisementRepository.save(advertisement);

        Map<String, Advertisement.Status> mapAdvertisements = BotStartConfig.getMapAdvertisements();
        mapAdvertisements.put(guildIdString, advertisement.getStatus());

        event.getHook().sendMessage("Реклама отключена. Помните что бесплатные проекты не могут жить вечно :)")
                .setEphemeral(true)
                .queue();
    }
}
