package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.*;

public class TaskTest {


    @Test
    @DisplayName("Задача: равенство задач определяется по id")
    public void equals_SameId_True(){
        Task t0 = newTaskWithId(1, TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task t1 = newTaskWithId(1, TASK_NAME_2, TASK_NAME_2, TaskStatus.IN_PROGRESS);

        assertEquals(t0, t1, "task entities should be compared by id");
    }

}
