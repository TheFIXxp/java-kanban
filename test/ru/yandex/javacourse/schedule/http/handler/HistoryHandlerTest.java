package ru.yandex.javacourse.schedule.http.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.StatusCode;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.newTask;

class HistoryHandlerTest extends HanderTest {

    @Test
    @DisplayName("GET /history — возвращает список истории просмотров задач и 200")
    void getHistory_ReturnsHistory() {
        Task task1 = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task task2 = newTask(TASK_NAME_2, TASK_DESC_2, TaskStatus.DONE);
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        HttpResponse<String> response = get(BASE_URL + "history");

        assertEquals(StatusCode.OK.getCode(), response.statusCode());

        List<Task> history = List.of(GSON.fromJson(response.body(), Task[].class));
        assertEquals(2, history.size());
        assertEquals(TASK_NAME_1, history.get(0).getName());
        assertEquals(TASK_NAME_2, history.get(1).getName());
    }

    @Test
    @DisplayName("GET /history — возвращает пустой список, если история пуста и 200")
    void getHistory_Empty_ReturnsEmptyList() {
        HttpResponse<String> response = get(BASE_URL + "history");
        assertEquals(StatusCode.OK.getCode(), response.statusCode());

        List<Task> history = List.of(GSON.fromJson(response.body(), Task[].class));
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("POST /history — возвращает 405")
    void postHistory_Returns405(){
        HttpResponse<String> response = post(BASE_URL + "history", "{}");
        assertEquals(StatusCode.METHOD_NOT_ALLOWED.getCode(), response.statusCode());

    }

    @Test
    @DisplayName("DELETE /history — возвращает 405")
    void deleteHistory_Returns405() {
        HttpResponse<String> response = delete(BASE_URL + "history");
        assertEquals(StatusCode.METHOD_NOT_ALLOWED.getCode(), response.statusCode());

    }
}

