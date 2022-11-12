package main.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NoticeRegistry {

    //Guild | List: userTrackerId | TrackingUser
    private static final ConcurrentMap<String, ConcurrentMap<String, TrackingUser>> trackingUserConcurrentMap = new ConcurrentHashMap<>();
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

    public void save(String guildId, ConcurrentMap<String, TrackingUser> concurrentMap) {
        trackingUserConcurrentMap.put(guildId, concurrentMap);
    }

    //                                                        TrackingUser | Data
    public void saveTrackingUser(String guildId, String user, TrackingUser trackingUser) {
        trackingUserConcurrentMap.get(guildId).put(user, trackingUser);
    }

    //TrackingUser | Data
    public ConcurrentMap<String, TrackingUser> get(String guildId) {
        return trackingUserConcurrentMap.get(guildId);
    }

    public TrackingUser getUser(String guildId, String user) {
        return trackingUserConcurrentMap.get(guildId).get(user);
    }

    public boolean hasGuild(String guildId) {
        return trackingUserConcurrentMap.containsKey(guildId);
    }

    public void removeGuild(String guildId) {
        trackingUserConcurrentMap.remove(guildId);
    }
}