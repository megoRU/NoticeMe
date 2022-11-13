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
@Table(name = "lock")
public class Lock {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lock_status", columnDefinition = "enum ('LOCKED', 'UNLOCKED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private Lock.Locked locked;

    public enum Locked {
        LOCKED,
        UNLOCKED
    }

}
