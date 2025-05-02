package main.jsonparser;

import lombok.AllArgsConstructor;
import main.config.BotStartConfig;
import main.model.entity.Language;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
public class ParserClass {

    public static final ConcurrentMap<String, String> russian = new ConcurrentHashMap<>();
    public static final ConcurrentMap<String, String> english = new ConcurrentHashMap<>();
    public static final ConcurrentMap<String, String> french = new ConcurrentHashMap<>();

    public String getTranslation(String key, String guildId) {
        Language.LanguageEnum languageEnum = BotStartConfig.getMapLanguages().get(guildId);
        if (languageEnum == null) languageEnum = Language.LanguageEnum.EN;

        return switch (languageEnum) {
            case EN -> english.get(key) == null ? "NO_FOUND_LOCALIZATION" : english.get(key);
            case RU -> russian.get(key) == null ? "NO_FOUND_LOCALIZATION" : russian.get(key);
            case FR -> french.get(key) == null ? "NO_FOUND_LOCALIZATION" : french.get(key);
        };
    }
}