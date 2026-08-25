package main.core.core;

import main.model.entity.Server;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class NoticeRegistry {

    // Guild | userTrackerId list | TrackingUser | Target User | Target User's subscribers
    private static final ConcurrentMap<Long, ConcurrentMap<Long, TrackingUser>> trackingUserConcurrentMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, Server> serverListMap = new ConcurrentHashMap<>();
    //Guild | List: user | Suggestions
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

    /**
     * Возвращает список пользователей, на которых подписан указанный пользователь в конкретной гильдии.
     *
     * @param guildId     ID гильдии.
     * @param referenceId ID пользователя (подписчика).
     * @return Множество ID отслеживаемых пользователей.
     */
    public Set<Long> getUserTrackerIdsByUserId(Long guildId, Long referenceId) {
        return trackingUserConcurrentMap.getOrDefault(guildId, new ConcurrentHashMap<>())
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getUserListSet().contains(referenceId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void save(Long guildId, ConcurrentMap<Long, TrackingUser> concurrentMap) {
        trackingUserConcurrentMap.put(guildId, concurrentMap);
    }

    /**
     * Подписывает пользователя на отслеживание другого пользователя в рамках указанного сервера.
     *
     * <p>Если подписка уже существует, повторно она не добавляется.
     * При отсутствии необходимых объектов {@code TrackingUser} они создаются автоматически.</p>
     *
     * @param guildId       ID сервера, в рамках которого создаётся подписка
     * @param userId        ID пользователя, который будет отслеживать
     * @param userIdTracker ID пользователя, которого необходимо отслеживать
     */
    public synchronized void sub(Long guildId, Long userId, Long userIdTracker) {
        if (!hasGuild(guildId)) {
            TrackingUser trackingUser = new TrackingUser();
            trackingUser.putUser(userId);
            ConcurrentMap<Long, TrackingUser> trackingUserConcurrentMap = new ConcurrentHashMap<>();
            trackingUserConcurrentMap.put(userIdTracker, trackingUser);
            save(guildId, trackingUserConcurrentMap);
        } else {
            TrackingUser trackingUserFromMap = getUser(guildId, userIdTracker);
            if (trackingUserFromMap == null) {
                TrackingUser trackingUser = new TrackingUser();
                trackingUser.putUser(userId);
                saveTrackingUser(guildId, userIdTracker, trackingUser);
            } else {
                get(guildId).get(userIdTracker).putUser(userId);
            }
        }
    }

    //TrackingUser | Data
    private void saveTrackingUser(Long guildId, Long user, TrackingUser trackingUser) {
        ConcurrentMap<Long, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap == null) {
            throw new IllegalArgumentException("In this collection does not exist Guild: saveTrackingUser()");
        }
        stringTrackingUserConcurrentMap.put(user, trackingUser);
    }

    //TrackingUser | Data
    private ConcurrentMap<Long, TrackingUser> get(Long guildId) {
        return trackingUserConcurrentMap.get(guildId);
    }

    public void putServer(Long serverId, Server server) {
        serverListMap.put(serverId, server);
    }

    public void removeServer(Long serverId) {
        serverListMap.remove(serverId);
    }

    @Nullable
    public Server getServer(Long serverId) {
        return serverListMap.get(serverId);
    }

    @Nullable
    public TrackingUser getUser(Long guildId, Long user) {
        ConcurrentMap<Long, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);

        if (stringTrackingUserConcurrentMap != null) {
            return stringTrackingUserConcurrentMap.get(user);
        } else {
            return null;
        }
    }

    private boolean hasGuild(Long guildId) {
        return trackingUserConcurrentMap.containsKey(guildId);
    }

    public void removeGuild(Long guildId) {
        trackingUserConcurrentMap.remove(guildId);
    }

    private void removeUserFromGuild(Long guildId, Long user) {
        ConcurrentMap<Long, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap != null) {
            stringTrackingUserConcurrentMap.remove(user);
        }
    }

    public void unsub(Long guildId, Long trackingUserId, Long userId) {
        ConcurrentMap<Long, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap != null) {
            TrackingUser trackingUser = stringTrackingUserConcurrentMap.get(trackingUserId);
            if (trackingUser != null) trackingUser.removeUserFromList(userId);
        }
    }

    public void removeUserFromAllGuild(Long user) {
        trackingUserConcurrentMap.forEach((guild, trackerUser) -> {
            TrackingUser trackingUser = trackerUser.get(user);
            if (trackingUser != null) {
                removeUserFromGuild(guild, user);
            }
        });
    }
}