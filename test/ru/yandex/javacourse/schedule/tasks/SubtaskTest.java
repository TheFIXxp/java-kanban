package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.*;

public class SubtaskTest {

    @Test
    @DisplayName("Подзадача: равенство подзадач определяется по id")
    public void equals_SameId_True(){
        Subtask s0 = newSubtaskWithId(1, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, 2);
        Subtask s1 = newSubtaskWithId(1, SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.IN_PROGRESS, 3);

        assertEquals(s0, s1, "task entities should be compared by id");
    }

    @Test
    @DisplayName("Подзадача: нельзя привязать подзадачу к самой себе")
    public void constructor_SelfEpicId_ThrowsIllegalArgument() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Subtask(1, "Subtask 1", "Testing subtask 1", TaskStatus.NEW, 1),
                "Subtask should not be able to attach to itself"
        );

        assertEquals("Subtask cannot be attached to itself", exception.getMessage());
    }
}
