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
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false)
    private Guild guildId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_tracking_id", nullable = false)
    private Long userTrackingId;

}