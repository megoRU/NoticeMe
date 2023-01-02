import main.core.NoticeRegistry;
import main.core.TrackingUser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
public class TestNoticeMe {

    private static final NoticeRegistry instance = NoticeRegistry.getInstance();

    @Test
    @Order(6)
    @DisplayName("Проверяем удаление отписывание")
    void testUnsubFromUser(){
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2000>, <@2500>", user.getUserList());
        instance.unsub("500", "3000", "2500");
        Assertions.assertEquals("<@2000>", user.getUserList());
    }

    @Test
    @Order(5)
    @DisplayName("Проверяем удаление Guild")
    void testDeleteGuild() {
        instance.removeGuild("4000");
    }

    @Test
    @Order(4)
    @DisplayName("Проверяем запрет на отслеживание")
    void testLock() {
        //Добавляем вторую гильдию
        instance.sub("600", "2000", "4000");

        //Удаляем везде 4000
        instance.removeUserFromAllGuild("4000");

        TrackingUser guild600User4000 = instance.getUser("600", "4000");
        TrackingUser guild500User4000 = instance.getUser("500", "4000");

        Assertions.assertNull(guild600User4000);
        Assertions.assertNull(guild500User4000);
    }

    @Test
    @DisplayName("Проверяем сохранение для другой Guild")
    void testSaveAnotherUser() {
        instance.sub("500", "2500", "4000");
    }

    @Test
    @Order(3)
    @DisplayName("Проверяем вывод при двух подписчиках")
    void testSaveMultiUsers() {
        instance.sub("500", "2500", "3000");
        //Пользователь 2500 и 2000 подписаны на пользователя 3000
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2000>, <@2500>", user.getUserList());
    }

    @Test
    @Order(1)
    @DisplayName("Проверяем вывод при одном подписчике")
    void testOneUser() {
        instance.sub("500", "2000", "3000");
        //Пользователь 2000 подписан на пользователя 3000
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("<@2000>", user.getUserList());
    }

    @Test
    @Order(2)
    @DisplayName("Проверяем метод hasUserJoin()")
    void testHasUserJoin() {
        TrackingUser user = instance.getUser("500", "3000");
        Assertions.assertNotNull(user);
        boolean first = user.hasUserJoin();
        boolean second = user.hasUserJoin();
        Assertions.assertFalse(first);
        Assertions.assertTrue(second);
    }

}

