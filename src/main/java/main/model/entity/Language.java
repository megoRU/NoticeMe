package main.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private LanguageEnum language;

    public enum LanguageEnum {
        RU,
        EN
    }
}
