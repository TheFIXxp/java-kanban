package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.*;
import ru.yandex.javacourse.schedule.exception.ManagerLoadException;
import ru.yandex.javacourse.schedule.tasks.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.javacourse.schedule.testdata.TestConstants.*;
import static ru.yandex.javacourse.schedule.testdata.TestDataFactory.*;

public class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    public void initManager() throws IOException {
        tempFile = File.createTempFile("test_tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    @DisplayName("Задачи: добавление новой задачи сохраняет и восстанавливает корректно")
    public void addNewTask_SaveAndLoad_TaskIsPersistedCorrectly() {
        Task task = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        manager.addNewTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> loadedTasks = loaded.getTasks();

        assertEquals(1, loadedTasks.size(), "Exactly one task should be loaded");
        Task loadedTask = loadedTasks.get(0);

        assertEquals(task.getName(), loadedTask.getName(), "Task name should match");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Task description should match");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Task status should match");
    }



    @Test
    @DisplayName("Файл: при IOException метод load() выбрасывает ManagerLoadException")
    public void load_WhenIOExceptionThrown_ThrowsManagerSaveException() {
        File readOnlyFile = new File("/root/readonly_tasks.csv");

        ManagerLoadException exception = assertThrows(
                ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(readOnlyFile),
                "Expected ManagerLoadException when load() fails"
        );

        assertTrue(
                exception.getMessage().startsWith("Ошибка при загрузке из файла"),
                "Exception message should mention loading error"
        );
    }

    @Test
    @DisplayName("Задачи: несколько задач сохраняются и восстанавливаются в том же порядке")
    public void saveAndLoad_MultipleTasks_PreserveOrder() {
        Task task1 = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task task2 = newTask(TASK_NAME_2, TASK_DESC_2, TaskStatus.IN_PROGRESS);
        Task task3 = newTask(TASK_NAME_3, TASK_DESC_3, TaskStatus.DONE);

        manager.addNewTask(task1);
        manager.addNewTask(task2);
        manager.addNewTask(task3);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = loaded.getTasks();

        assertEquals(3, tasks.size(), "Three tasks should be loaded");
        assertEquals(TaskStatus.NEW, tasks.get(0).getStatus(), "First task should have status NEW");
        assertEquals(TaskStatus.IN_PROGRESS, tasks.get(1).getStatus(), "Second task should have status IN_PROGRESS");
        assertEquals(TaskStatus.DONE, tasks.get(2).getStatus(), "Third task should have status DONE");
    }

    @Test
    @DisplayName("Файл: после загрузки generatorId совпадает с последним ID")
    public void loadFromFile_RestoresGeneratorId() {
        Task task1 = newTask(TASK_NAME_1, TASK_DESC_1, TaskStatus.NEW);
        Task task2 = newTask(TASK_NAME_2, TASK_DESC_2, TaskStatus.IN_PROGRESS);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertEquals(2, manager.getGeneratorId(), "Generator ID before saving should be 2");

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(2, loaded.getGeneratorId(), "Generator ID after loading should be 2");
    }

    @Test
    @DisplayName("Файл: при пустом менеджере создаётся пустой файл (только заголовок)")
    public void save_EmptyManager_CreatesOnlyHeader() throws IOException {
        manager.save();
        String content = Files.readString(tempFile.toPath());

        assertTrue(content.isEmpty() || content.startsWith("id,"), "File should be empty or contain only header");
    }

    @Test
    @DisplayName("Файл: загрузка из пустого файла создаёт пустой менеджер")
    public void load_EmptyFile_ReturnsEmptyManager() throws IOException {
        Files.writeString(tempFile.toPath(), "");
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getTasks().isEmpty(), "Manager should be empty after loading from empty file");
    }

    @Test
    @DisplayName("Эпики: сохранение и загрузка восстанавливает эпики и подзадачи")
    public void saveAndLoad_EpicsAndSubtasks_RestoreStructureAndIds() {
        Epic epic = newEpicWithId(1,EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask sub1 = newSubtaskWithId(2,SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        Subtask sub2 = newSubtaskWithId(3,SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.IN_PROGRESS, epic.getId());
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Epic> loadedEpics = loaded.getEpics();
        List<Subtask> loadedSubs = loaded.getSubtasks();

        assertEquals(1, loadedEpics.size(), "One epic should be loaded");
        assertEquals(2, loadedSubs.size(), "Two subtasks should be loaded");

        Epic loadedEpic = loadedEpics.get(0);
        assertEquals(epic.getName(), loadedEpic.getName(), "Epic name should match");
        assertEquals(epic.getDescription(), loadedEpic.getDescription(), "Epic description should match");

        assertEquals(2, loadedEpic.getSubtaskIds().size(), "Epic should contain 2 subtask IDs");
        assertEquals(loadedEpic.getId(), loadedSubs.get(0).getEpicId(), "Subtask 1 should reference its epic");
        assertEquals(loadedEpic.getId(), loadedSubs.get(1).getEpicId(), "Subtask 2 should reference its epic");

        assertEquals(3, loaded.getGeneratorId(), "Generator ID should be 3 after loading");
    }

    @Test
    @DisplayName("Эпики: удаление эпика удаляет связанные подзадачи и сохраняется корректно")
    public void deleteEpic_RemovesLinkedSubtasks_AndPersists() {
        Epic epic = newEpicWithId(1,EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask sub1 = newSubtaskWithId(2,SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        Subtask sub2 = newSubtaskWithId(2,SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        assertEquals(2, manager.getSubtasks().size(), "Two subtasks should exist before deletion");

        manager.deleteEpic(epic.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getEpics().isEmpty(), "Epics should be empty after deletion");
        assertTrue(loaded.getSubtasks().isEmpty(), "Subtasks should be empty after deleting their epic");
    }

    @Test
    @DisplayName("Подзадачи: удаление подзадачи обновляет связи в эпике и сохраняется в файле")
    public void deleteSubtask_UpdatesEpicRelations_AndPersists() {
        Epic epic = newEpicWithId(1,EPIC_NAME_1, EPIC_DESC_1);
        manager.addNewEpic(epic);

        Subtask sub1 = newSubtaskWithId(2,SUBTASK_NAME_1, SUBTASK_DESC_1, TaskStatus.NEW, epic.getId());
        Subtask sub2 = newSubtaskWithId(3,SUBTASK_NAME_2, SUBTASK_DESC_2, TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        manager.deleteSubtask(sub1.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loaded.getEpics().get(0);

        assertEquals(1, loadedEpic.getSubtaskIds().size(), "Epic should have 1 subtask after deletion");
        assertFalse(loadedEpic.getSubtaskIds().contains(sub1.getId()), "Deleted subtask ID should not be present in epic");
    }
}
