package main.jsonparser;

import main.model.entity.Language;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ParserClass {

    public static final ConcurrentMap<String, String> russian = new ConcurrentHashMap<>();
    public static final ConcurrentMap<String, String> english = new ConcurrentHashMap<>();
    private static volatile ParserClass parserClass;

    private ParserClass() {
    }

    public static ParserClass getInstance() {
        if (parserClass == null) {
            synchronized (ParserClass.class) {
                if (parserClass == null) {
                    parserClass = new ParserClass();
                }
            }
        }
        return parserClass;
    }

    public String getTranslation(String key, Language.LanguageEnum languageEnum) {
        try {
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
