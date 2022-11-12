package main.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "server")
public class Server {

    @Id
    @Column(name = "guild_id_long", nullable = false)
    private Long guildIdLong;

    @Column(name = "channel_id", nullable = false)
    private Long textChannelId;
}
