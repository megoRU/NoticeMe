package main.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NoticeRegistry {

    //Guild | List: userTrackerId | TrackingUser
    public static final ConcurrentMap<String, ConcurrentMap<String, TrackingUser>> trackingUserConcurrentMap = new ConcurrentHashMap<>();
    private static volatile NoticeRegistry noticeRegistry;

    private NoticeRegistry() {
    }

    public static NoticeRegistry getInstance() {
        if (noticeRegistry == null) {
            synchronized (NoticeRegistry.class) {
                if (noticeRegistry == null) {
                    noticeRegistry = new NoticeRegistry();
                }
            }
        }
        return noticeRegistry;
    }

    public ConcurrentMap<String, ConcurrentMap<String, TrackingUser>> getTrackingUserConcurrentMap() {
        return trackingUserConcurrentMap;
    }

    public ConcurrentMap<String, TrackingUser> getTrakerUser(String guildId) {
        return trackingUserConcurrentMap.get(guildId);
    }


}
