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
@Table(name = "subs")
public class Subs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id_long", nullable = false)
    private Server server;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_tracking_id", nullable = false)
    private String userTrackingId;

}