package main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
