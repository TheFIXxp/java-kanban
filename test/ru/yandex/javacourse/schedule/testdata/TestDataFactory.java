package ru.yandex.javacourse.schedule.testdata;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class TestDataFactory {

    public static Task newTask(String name, String desc, TaskStatus status) {
        return new Task(name, desc, status);
    }

    public static Epic newEpicWithId(int id, String name, String desc) {
        return new Epic(id, name, desc);
    }

    public static Subtask newSubtaskWithId(int id, String name, String desc, TaskStatus status, int epicId) {
        return new Subtask(id, name, desc, status, epicId);
    }

    public static Task newTaskWithId(int id, String name, String desc, TaskStatus status) {
        return new Task(id, name, desc, status);
    }
}
