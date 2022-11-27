package main.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.sql.Timestamp;
import java.util.List;

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
        for (Member listMember : listMembers) {
            if (listMember.getUser().getIdLong() != userId) {
                if (stringBuilder.isEmpty()) {
                    stringBuilder.append(listMember.getUser().getId());
                } else {
                    stringBuilder.append(",").append(listMember.getUser().getId());
                }
            }
        }
        if (stringBuilder.isEmpty()) this.usersInChannel = null;
        else this.usersInChannel = stringBuilder.toString();
    }
}
