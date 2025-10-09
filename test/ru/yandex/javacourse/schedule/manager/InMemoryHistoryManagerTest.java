package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.*;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("История: при повторном добавлении той же задачи сохраняется только одна запись")
    public void addTask_SameInstanceTwice_OnlyOneUpdatedEntryInHistory() {
        Task task = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "Task should be stored");

        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);

        assertEquals(1, historyManager.getHistory().size(), "History should contain only one task after update");
        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().get(0).getStatus(), "Only the updated task should remain in history");
    }

    @Test
    @DisplayName("История: удаление задачи по id убирает её из истории и сохраняет остальные")
    public void remove_TaskById_TaskRemovedFromHistory() {
        Task task1 = newTaskWithId(1, TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task task2 = newTaskWithId(2, TASK_NAME_2, TASK_DESC_2, TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);

        assertEquals(2, historyManager.getHistory().size(), "History should contain 2 tasks");

        historyManager.remove(task1.getId());

        assertEquals(1, historyManager.getHistory().size(), "History should contain 1 task after removal");
        assertEquals(task2.getStatus(), historyManager.getHistory().get(0).getStatus(), "Only task2 should remain in history");
    }

    @Test
    @DisplayName("История: можно добавить новую задачу после удаления предыдущей")
    public void addTask_AfterRemoval_NewTaskAppearsAloneInHistory() {
        Task task1 = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task task2 = newTask(TASK_NAME_2, TASK_DESC_2, TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.remove(task1.getId());

        historyManager.addTask(task2);

        assertEquals(1, historyManager.getHistory().size(), "History should contain only task2");
        assertEquals(task2.getStatus(), historyManager.getHistory().get(0).getStatus(), "Task2 should be the only task in history");
    }

    @Test
    @DisplayName("История: порядок задач соответствует порядку добавления")
    public void getHistory_AddThreeTasks_OrderIsPreserved() {
        Task task1 = newTaskWithId(1, TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task task2 = newTaskWithId(2, TASK_NAME_2, TASK_DESC_2, TaskStatus.NEW);
        Task task3 = newTaskWithId(3, TASK_NAME_3, TASK_DESC_3, TaskStatus.NEW);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        assertEquals(task1.getStatus(), historyManager.getHistory().get(0).getStatus(), "The first task in history should be task1");
        assertEquals(task2.getStatus(), historyManager.getHistory().get(1).getStatus(), "The second task in history should be task2");
        assertEquals(task3.getStatus(), historyManager.getHistory().get(2).getStatus(), "The third task in history should be task3");
    }

}
