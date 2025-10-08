package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }


    @Test
    public void testHistoricVersionsByPointer() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.addTask(task);

        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "Task should be stored");

        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);

        assertEquals(1, historyManager.getHistory().size(), "History should contain only one task after update");
        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().get(0).getStatus(), "Only the updated task should remain in history");
    }

    @Test
    public void testRemoveTaskFromHistory() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);

        historyManager.addTask(task1);
        historyManager.addTask(task2);

        assertEquals(2, historyManager.getHistory().size(), "History should contain 2 tasks");

        historyManager.remove(task1.getId());

        assertEquals(1, historyManager.getHistory().size(), "History should contain 1 task after removal");
        assertEquals(task2.getStatus(), historyManager.getHistory().get(0).getStatus(), "Only task2 should remain in history");
    }

    @Test
    public void testAddTaskAfterRemoval() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);

        historyManager.addTask(task1);
        historyManager.remove(task1.getId());

        historyManager.addTask(task2);

        assertEquals(1, historyManager.getHistory().size(), "History should contain only task2");
        assertEquals(task2.getStatus(), historyManager.getHistory().get(0).getStatus(), "Task2 should be the only task in history");
    }

    @Test
    public void testHistoryOrder() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        assertEquals(task1.getStatus(), historyManager.getHistory().get(0).getStatus(), "The first task in history should be task1");
        assertEquals(task2.getStatus(), historyManager.getHistory().get(1).getStatus(), "The second task in history should be task2");
        assertEquals(task3.getStatus(), historyManager.getHistory().get(2).getStatus(), "The third task in history should be task3");
    }

}
