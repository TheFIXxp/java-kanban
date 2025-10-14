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

class EpicsHandlerTest extends HanderTest {

    @Test
    @DisplayName("GET /epics — возвращает все эпики и возвращает 200")
    void getAllEpics_ReturnsAllEpics() {
        Epic e1 = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        Epic e2 = newEpicWithId(2, EPIC_NAME_2, EPIC_DESC_2);
        manager.addNewEpic(e1);
        manager.addNewEpic(e2);

        HttpResponse<String> response = get(BASE_URL + "epics");

        assertEquals(200, response.statusCode());
        List<Epic> epics = List.of(GSON.fromJson(response.body(), Epic[].class));
        assertEquals(2, epics.size());
        assertEquals(EPIC_NAME_1, epics.get(0).getName());
    }

    @Test
    @DisplayName("GET /epics/{id} — возвращает эпик по ID и возвращает 200")
    void getEpicById_ReturnsEpic() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        HttpResponse<String> response = get(BASE_URL + "epics/" + epic.getId());

        assertEquals(200, response.statusCode());
        Epic returned = GSON.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), returned.getId());
        assertEquals(EPIC_NAME_1, returned.getName());
    }

    @Test
    @DisplayName("GET /epics/{id}/subtasks — возвращает все подзадачи эпика и 200")
    void getEpicSubtasks_ReturnsAllLinkedSubtasks() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask s1 = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        Subtask s2 = newSubtaskWithId(3, SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.DONE, epic.getId());
        manager.addNewSubtask(s1);
        manager.addNewSubtask(s2);

        HttpResponse<String> response = get(BASE_URL + "epics/" + epic.getId() + "/subtasks");

        assertEquals(200, response.statusCode());
        List<Subtask> subtasks = List.of(GSON.fromJson(response.body(), Subtask[].class));
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals(SUBTASK_NAME_1)));
    }

    @Test
    @DisplayName("GET /epics/{id} — несуществующий ID возвращает 404")
    void getEpicById_NotFound() {
        HttpResponse<String> response = get(BASE_URL + "epics/999");
        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("POST /epics — создаёт новый эпик")
    void postEpic_CreatesNewEpic() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        String json = GSON.toJson(epic);

        HttpResponse<String> response = post(BASE_URL + "epics", json);

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getEpics().size());
    }

    @Test
    @DisplayName("DELETE /epics/{id} — удаляет эпик и его подзадачи и возвращает 200")
    void deleteEpicById_RemovesEpicAndSubtasks() {
        Epic epic = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask s1 = newSubtaskWithId(2, SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        Subtask s2 = newSubtaskWithId(3, SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(s1);
        manager.addNewSubtask(s2);

        HttpResponse<String> response = delete(BASE_URL + "epics/" + epic.getId());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    @DisplayName("DELETE /epics — без ID возвращает 400")
    void deleteWithoutId_Returns400() {
        HttpResponse<String> response = delete(BASE_URL + "epics");
        assertEquals(400, response.statusCode());
    }


}

