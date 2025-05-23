import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@DisplayName("TestNoticeMe")
public class TestNoticeMe {

    private static final NoticeRegistry instance = NoticeRegistry.getInstance();

    @BeforeEach
    void setUp() {
        instance.removeGuild("500");
        instance.removeGuild("600");
    }

    @AfterEach
    void tearDown() {
        instance.removeGuild("500");
        instance.removeGuild("600");
    }

    @Test
    @DisplayName("Проверяем отписывание")
    void testUnsubFromUser() {
        instance.sub("500", "3000", "2500");
        TrackingUser user = instance.getUser("500", "2500");

        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@3000>", user.getUserList());

        instance.unsub("500", "2500", "3000");

        Assertions.assertEquals("", user.getUserList());
    }

    @Test
    @DisplayName("Проверяем вывод пользователей кто подписан на него")
    void testSubscribersByReferenceId() {
        instance.sub("500", "3000", "2500");
        instance.sub("500", "3000", "2501");
        instance.sub("500", "3000", "2502");

        Set<String> subscribersSetByUserId = instance.getAllUserTrackerIdsByUserId("500", "3000");

        List<String> actualList = new ArrayList<>(subscribersSetByUserId);
        actualList.sort(Comparator.naturalOrder());

        Assertions.assertArrayEquals(new String[]{"2500", "2501", "2502"}, actualList.toArray());
    }

    @Test
    @DisplayName("Проверяем вывод предложений для пользователя")
    void testSubscribersUser() {
        instance.addUserSuggestions("500", "3000", "2500");
        instance.addUserSuggestions("500", "3000", "2501");
        instance.addUserSuggestions("500", "3001", "2502");

        Set<String> suggestionsList = instance.getSuggestionsList("500", "3000");

        List<String> actualList = new ArrayList<>(suggestionsList);
        actualList.sort(Comparator.naturalOrder());

        Assertions.assertArrayEquals(new String[]{"2500", "2501"}, actualList.toArray());
    }

    @Test
    @DisplayName("Проверяем удаление Guild")
    void testDeleteGuild() {
        instance.sub("4000", "3000", "2500");
        instance.removeGuild("4000");

        TrackingUser user = instance.getUser("4000", "2500");

        Assertions.assertNull(user);
    }

    @Test
    @DisplayName("Проверяем сохранение для другой Guild")
    void testSaveAnotherUser() {
        instance.sub("500", "2500", "4000");
        instance.sub("600", "3000", "2500");
        instance.sub("500", "2500", "3300");

        TrackingUser user = instance.getUser("500", "4000");
        TrackingUser secondUser = instance.getUser("600", "2500");

        Assertions.assertNotNull(user);
        Assertions.assertNotNull(secondUser);
        Assertions.assertEquals("<@2500>", user.getUserList());
        Assertions.assertEquals("<@3000>", secondUser.getUserList());
    }

    @Test
    @DisplayName("Проверяем вывод при двух подписчиках")
    void testSaveMultiUsers() {
        instance.sub("500", "2500", "3000");
        instance.sub("500", "2600", "3000");

        //Пользователь 2500 и 2000 подписаны на пользователя 3000
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2500>, <@2600>", user.getUserList());
    }

    @Test
    @DisplayName("Проверяем удаление пользователя из всех Guild")
    void testDeletingUserFromAllGuilds() {
        instance.sub("500", "2500", "3000");
        instance.sub("500", "2501", "3000");
        instance.sub("500", "2502", "3000");

        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals(3, user.getUserCount());

        instance.removeUserFromAllGuild("3000");
        Set<String> stringSet = instance.getAllUserTrackerIdsByUserId("500", "3000");

        Assertions.assertEquals(0, stringSet.size());
    }

    @Test
    @DisplayName("Проверяем метод hasUserJoin()")
    void testHasUserJoin() {
        instance.sub("500", "2500", "3000");
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        boolean first = user.hasUserJoin();
        boolean second = user.hasUserJoin();
        Assertions.assertFalse(first);
        Assertions.assertTrue(second);
    }

    @Test
    @DisplayName("Проверяем вывод при одном подписчике")
    void testOneUser() {
        instance.sub("500", "2000", "3000");
        //Пользователь 2000 подписан на пользователя 3000
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2000>", user.getUserList());
    }
}