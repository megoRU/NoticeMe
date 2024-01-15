import main.core.core.NoticeRegistry;
import main.core.core.TrackingUser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
public class TestNoticeMe {

    private static final NoticeRegistry instance = NoticeRegistry.getInstance();

    @BeforeEach
    void setUp() {
        instance.removeGuild("500");
        instance.removeGuild("600");
    }

    @Test
    @DisplayName("Проверяем удаление отписывание")
    void testUnsubFromUser() {
        instance.sub("500", "3000", "2500");
        TrackingUser user = instance.getUser("500", "2500");

        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@3000>", user.getUserList());

        instance.unsub("500", "2500", "3000");

        Assertions.assertEquals("", user.getUserList());
    }

    @Test
    @DisplayName("Проверяем удаление Guild")
    void testDeleteGuild() {
        instance.removeGuild("4000");

        TrackingUser user = instance.getUser("4000", "2500");

        Assertions.assertNull(user);
    }

    @Test
    @DisplayName("Проверяем запрет на отслеживание")
    void testLock() {
        //Добавляем вторую гильдию
        instance.sub("600", "2000", "4000");

        //Удаляем везде 4000
        instance.removeUserFromAllGuild("4000");

        TrackingUser guild600User4000 = instance.getUser("600", "4000");

        Assertions.assertNull(guild600User4000);
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
        //Пользователь 2500 и 2000 подписаны на пользователя 3000
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2500>", user.getUserList());
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