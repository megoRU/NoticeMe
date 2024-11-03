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
@Table(name = "language")
public class Language {

    @Id
    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "language", columnDefinition = "enum ('RU', 'EN')", nullable = false)
    @Enumerated(EnumType.STRING)
    private LanguageEnum language;

    public enum LanguageEnum {
        RU,
        EN
    }
}
