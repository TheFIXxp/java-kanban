package ru.yandex.javacourse.schedule.http.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.newTask;

class TasksHandlerTest extends HanderTest {

    @Test
    @DisplayName("GET /tasks — возвращает все задачи и 200")
    void getAllTasks_ReturnsAllTasks() {
        Task t1 = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task t2 = newTask(TASK_NAME_2, TASK_DESC_2, TaskStatus.DONE);
        manager.addNewTask(t1);
        manager.addNewTask(t2);

        HttpResponse<String> response = get(BASE_URL + "tasks");

        assertEquals(200, response.statusCode());

        List<Task> tasks = List.of(GSON.fromJson(response.body(), Task[].class));
        assertEquals(2, tasks.size());
        assertEquals(TASK_NAME_1, tasks.get(0).getName());
    }

    @Test
    @DisplayName("GET /tasks/{id} — возвращает задачу по ID и 200")
    void getTaskById_ReturnsTask() {
        Task task = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        manager.addNewTask(task);

        HttpResponse<String> response = get(BASE_URL + "tasks/" + task.getId());

        assertEquals(200, response.statusCode());

        Task returned = GSON.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), returned.getId());
        assertEquals(TASK_NAME_1, returned.getName());
    }

    @Test
    @DisplayName("GET /tasks/{id} — несуществующий ID возвращает и код ошибки 404")
    void getTaskById_NotFound() {
        HttpResponse<String> response = get(BASE_URL + "tasks/999");

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("POST /tasks — создаёт новую задачу и 201")
    void postTask_CreatesNewTask() {
        Task task = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        String json = GSON.toJson(task);

        HttpResponse<String> response = post(BASE_URL + "tasks", json);

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    @DisplayName("POST /tasks/{id} — обновляет задачу и 201")
    void postTaskById_UpdatesTask() {
        Task task = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        manager.addNewTask(task);

        task.setName(TASK_NAME_2);
        String json = GSON.toJson(task);

        HttpResponse<String> response = post(BASE_URL + "tasks/" + task.getId(), json);

        assertEquals(201, response.statusCode());
        assertEquals(TASK_NAME_2, manager.getTask(task.getId()).getName());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} — удаляет задачу и 200")
    void deleteTaskById_RemovesTask() {
        Task task = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        manager.addNewTask(task);

        HttpResponse<String> response = delete(BASE_URL + "tasks/" + task.getId());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    @DisplayName("DELETE /tasks — без ID возвращает 400")
    void deleteWithoutId_Returns400() {
        HttpResponse<String> response = delete(BASE_URL + "tasks");

        assertEquals(400, response.statusCode());
    }


    @Test
    @DisplayName("POST /tasks — при пересечении задач возвращает 406")
    void postTask_WithTimeIntersection_Returns406() {
        Task t1 = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        t1.setStartTime(TASK_START_TIME_1);
        t1.setDuration(TASK_DURATION_1);
        manager.addNewTask(t1);

        Task t2 = newTask(TASK_NAME_2, TASK_DESC_2, TaskStatus.NEW);
        t2.setStartTime(TASK_START_TIME_1);
        t2.setDuration(TASK_DURATION_1);

        String json = GSON.toJson(t2);
        HttpResponse<String> response = post(BASE_URL + "tasks", json);

        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }
}
