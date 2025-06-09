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
@Table(name = "gender")
public class Gender {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "gender", columnDefinition = "enum ('MALE', 'FEMALE')", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender.GenderType gender;

    public enum GenderType {
        MALE,
        FEMALE,
    }
}