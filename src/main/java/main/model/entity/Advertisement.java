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
@Table(name = "advertisement")
public class Advertisement {

    @Id
    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "status", columnDefinition = "enum ('DISABLED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        DISABLED
    }
}