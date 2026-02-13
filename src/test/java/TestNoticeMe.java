import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TestNoticeMe")
public class TestNoticeMe {

    private static final NoticeRegistry instance = NoticeRegistry.getInstance();

    @BeforeEach
    void setUp() {
        instance.removeGuild(500L);
        instance.removeGuild(600L);
    }

    @AfterEach
    void tearDown() {
        instance.removeGuild(500L);
        instance.removeGuild(600L);
    }

    @Test
    @DisplayName("Проверяем отписывание")
    void testUnsubFromUser() {
        instance.sub(500L, 3000L, 2500L);
        TrackingUser user = instance.getUser(500L, 2500L);

        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@3000>", user.getUserList());

        instance.unsub(500L, 2500L, 3000L);

        Assertions.assertEquals("", user.getUserList());
    }

    @Test
    @DisplayName("Проверяем вывод пользователей кто подписан на него")
    void testSubscribersByReferenceId() {
        instance.sub(500L, 3000L, 2500L);
        instance.sub(500L, 3000L, 2501L);
        instance.sub(500L, 3000L, 2502L);

        Set<Long> subscribersSetByUserId = instance.getUserTrackerIdsByUserId(500L, 3000L);

        List<Long> actualList = new ArrayList<>(subscribersSetByUserId);
        actualList.sort(Comparator.naturalOrder());

        Assertions.assertArrayEquals(new Long[]{2500L, 2501L, 2502L}, actualList.toArray());
    }

    @Test
    @DisplayName("Проверяем удаление Guild")
    void testDeleteGuild() {
        instance.sub(4000L, 3000L, 2500L);
        instance.removeGuild(4000L);

        TrackingUser user = instance.getUser(4000L, 2500L);

        Assertions.assertNull(user);
    }

    @Test
    @DisplayName("Проверяем сохранение для другой Guild")
    void testSaveAnotherUser() {
        instance.sub(500L, 2500L, 4000L);
        instance.sub(600L, 3000L, 2500L);
        instance.sub(500L, 2500L, 3300L);

        TrackingUser user = instance.getUser(500L, 4000L);
        TrackingUser secondUser = instance.getUser(600L, 2500L);

        Assertions.assertNotNull(user);
        Assertions.assertNotNull(secondUser);
        Assertions.assertEquals("<@2500>", user.getUserList());
        Assertions.assertEquals("<@3000>", secondUser.getUserList());
    }

    @Test
    @DisplayName("Проверяем вывод при двух подписчиках")
    void testSaveMultiUsers() {
        instance.sub(500L, 2500L, 3000L);
        instance.sub(500L, 2600L, 3000L);

        //Пользователь 2500 и 2000 подписаны на пользователя 3000
        TrackingUser user = instance.getUser(500L, 3000L);
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2500>, <@2600>", user.getUserList());
    }

    @Test
    @DisplayName("Проверяем удаление пользователя из всех Guild")
    void testDeletingUserFromAllGuilds() {
        instance.sub(500L, 2500L, 3000L);
        instance.sub(500L, 2501L, 3000L);
        instance.sub(500L, 2502L, 3000L);

        TrackingUser user = instance.getUser(500L, 3000L);
        Assertions.assertNotNull(user);
        Assertions.assertEquals(3, user.getUserCount());

        instance.removeUserFromAllGuild(3000L);
        Set<Long> stringSet = instance.getUserTrackerIdsByUserId(500L, 3000L);

        Assertions.assertEquals(0, stringSet.size());
    }

    @Test
    @DisplayName("Проверяем метод hasUserJoin()")
    void testHasUserJoin() {
        instance.sub(500L, 2500L, 3000L);
        TrackingUser user = instance.getUser(500L, 3000L);
        Assertions.assertNotNull(user);
        boolean first = user.hasUserJoin();
        boolean second = user.hasUserJoin();
        Assertions.assertFalse(first);
        assertTrue(second);
    }

    @Test
    @DisplayName("Проверяем вывод при одном подписчике")
    void testOneUser() {
        instance.sub(500L, 2000L, 3000L);
        //Пользователь 2000 подписан на пользователя 3000
        TrackingUser user = instance.getUser(500L, 3000L);
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2000>", user.getUserList());
    }
}