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

    public String getTranslation(String key, String guildId) {
        Language.LanguageEnum languageEnum = BotStartConfig.getMapLanguages().get(guildId);
        if (languageEnum == null) languageEnum = Language.LanguageEnum.EN;
        if (languageEnum.equals(Language.LanguageEnum.EN)) {
            return english.get(key) == null ? "NO_FOUND_LOCALIZATION" : english.get(key);
        } else {
            return russian.get(key) == null ? "NO_FOUND_LOCALIZATION" : russian.get(key);
        }
    }
}