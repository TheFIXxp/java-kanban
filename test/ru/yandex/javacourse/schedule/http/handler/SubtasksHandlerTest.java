package ru.yandex.javacourse.schedule.http.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.newEpicWithId;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.newSubtaskWithId;

class SubtasksHandlerTest extends HanderTest {

    @Test
    @DisplayName("GET /subtasks — возвращает все подзадачи и возвращает 200")
    void getAllSubtasks_ReturnsAllSubtasks() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask s1 = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        Subtask s2 = newSubtaskWithId(3, SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.DONE, epic.getId());
        manager.addNewSubtask(s1);
        manager.addNewSubtask(s2);

        HttpResponse<String> response = get(BASE_URL + "subtasks");

        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = List.of(GSON.fromJson(response.body(), Subtask[].class));
        assertEquals(2, subtasks.size());
        assertEquals(SUBTASK_NAME_1, subtasks.get(0).getName());
    }

    @Test
    @DisplayName("GET /subtasks/{id} — возвращает подзадачу по ID и возвращает 200")
    void getSubtaskById_ReturnsSubtask() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask subtask = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask);

        HttpResponse<String> response = get(BASE_URL + "subtasks/" + subtask.getId());

        assertEquals(200, response.statusCode());

        Subtask returned = GSON.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), returned.getId());
        assertEquals(SUBTASK_NAME_1, returned.getName());
    }

    @Test
    @DisplayName("GET /subtasks/{id} — несуществующий ID возвращает 404")
    void getSubtaskById_NotFound() {
        HttpResponse<String> response = get(BASE_URL + "subtasks/999");

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("POST /subtasks — создаёт новую подзадачу и возвращает 201")
    void postSubtask_CreatesNewSubtask() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask subtask = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        String json = GSON.toJson(subtask);

        HttpResponse<String> response = post(BASE_URL + "subtasks", json);

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getSubtasks().size());
    }

    @Test
    @DisplayName("POST /subtasks/{id} — обновляет подзадачу и возвращает 201")
    void postSubtaskById_UpdatesSubtask() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask subtask = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask);

        subtask.setName(SUBTASK_NAME_2);
        String json = GSON.toJson(subtask);

        HttpResponse<String> response = post(BASE_URL + "subtasks/" + subtask.getId(), json);

        assertEquals(201, response.statusCode());
        assertEquals(SUBTASK_NAME_2, manager.getSubtask(subtask.getId()).getName());
    }

    @Test
    @DisplayName("DELETE /subtasks/{id} — удаляет подзадачу и возвращает 200")
    void deleteSubtaskById_RemovesSubtask() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask subtask = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask);

        HttpResponse<String> response = delete(BASE_URL + "subtasks/" + subtask.getId());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    @DisplayName("DELETE /subtasks — без ID возвращает 400")
    void deleteWithoutId_Returns400() {
        HttpResponse<String> response = delete(BASE_URL + "subtasks");

        assertEquals(400, response.statusCode());
    }
}

