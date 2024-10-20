package main.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.config.BotStartConfig;
import net.dv8tion.jda.api.entities.Member;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "entries")
public class Entries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "users_in_channel")
    private String usersInChannel;

    @Column(name = "join_time", nullable = false)
    private Timestamp joinTime;

    public void setUsersInChannel(List<Member> listMembers) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Lock.Locked> mapLocks = BotStartConfig.getMapLocks();
        listMembers.stream()
                .filter(member -> member.getUser().getIdLong() != userId)
                .filter(member -> !mapLocks.containsKey(member.getUser().getId()))
                .forEach(member -> {
                            if (stringBuilder.isEmpty()) {
                                stringBuilder.append(member.getUser().getId());
                            } else {
                                stringBuilder.append(",").append(member.getUser().getId());
                            }
                        }
                );
        if (stringBuilder.isEmpty()) this.usersInChannel = null;
        else this.usersInChannel = stringBuilder.toString();
    }
}
