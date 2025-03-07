package main.config;

public class Config {

    private static final String DEV_BOT_TOKEN = System.getenv("DEV_TOKEN");
    private static final String PRODUCTION_BOT_TOKEN = System.getenv("PROD_TOKEN");
    private static final String TOKEN = PRODUCTION_BOT_TOKEN;
    private static final String TOP_GG_API_TOKEN = System.getenv("TOP_GG_API_TOKEN");
    private static final String BOT_ID = "1039911109911658557"; //megoDev: 780145910764142613 //giveaway: 808277484524011531
    private static final String URL = "https://discord.com/oauth2/authorize?client_id=1039911109911658557&permissions=2147502080&scope=applications.commands%20bot";
    private static volatile boolean IS_DEV = true;

    static {
        if (TOKEN.equals(PRODUCTION_BOT_TOKEN)) {
            IS_DEV = false;
        }
    }

    public static String getTOKEN() {
        return TOKEN;
    }

    public static String getTopGgApiToken() {
        return TOP_GG_API_TOKEN;
    }

    public static String getBotId() {
        return BOT_ID;
    }

    public static boolean isIsDev() {
        return IS_DEV;
    }
}