package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.*;

public class EpicTest {

    @Test
    @DisplayName("Эпик: равенство эпиков определяется по id")
    public void equals_SameId_True() {
        Epic e0 = newEpicWithId(1, EPIC_NAME_1, EPIC_DESC_1);
        Epic e1 = newEpicWithId(1, EPIC_NAME_2, EPIC_DESC_2);

        assertEquals(e0, e1, "task and subentities should be compared by id");
    }

    @Test
    @DisplayName("Эпик: список подзадач хранит только уникальные id")
    public void addSubtaskId_Duplicates_NotAdded() {
        Epic epic = new Epic(0, EPIC_NAME_1, EPIC_DESC_1);

        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        assertEquals(2, epic.subtaskIds.size(), "should add distinct subtask ids");

        epic.addSubtaskId(1);

        assertEquals(2, epic.subtaskIds.size(), "should not add same subtask id twice");
    }

    @Test
    @DisplayName("Эпик: нельзя привязать эпик к самому себе")
    public void addSubtaskId_Self_NotAdded() {
        Epic epic = new Epic(0, EPIC_NAME_1, EPIC_DESC_1);

        epic.addSubtaskId(0);

        assertEquals(0, epic.subtaskIds.size(), "epic should not add itself as subtask");
    }
}
