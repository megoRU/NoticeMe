package main.model.entity;

import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Subs> subscriptions;
}
