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
        try {
            Language.LanguageEnum languageEnum = BotStartConfig.mapLanguages.get(guildId);
            if (languageEnum == null) languageEnum = Language.LanguageEnum.EN;
            if (languageEnum.equals(Language.LanguageEnum.EN)) {
                return english.get(key);
            } else {
                return russian.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NO_FOUND_LOCALIZATION";
    }
}
