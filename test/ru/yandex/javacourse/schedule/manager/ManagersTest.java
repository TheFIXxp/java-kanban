package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test
    @DisplayName("Менеджеры: фабричные методы возвращают не null экземпляры")
    public void getDefaultManagers_WhenCalled_NotNull() {
        TaskManager defaultManager = Managers.getDefault();
        HistoryManager defaultHistory = Managers.getDefaultHistory();

        assertNotNull(defaultManager, "default manager should not be null");
        assertNotNull(defaultHistory, "default history managers should not be null");
    }

}
