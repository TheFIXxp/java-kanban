package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.exception.ManagerLoadException;
import ru.yandex.javacourse.schedule.exception.ManagerSaveException;
import ru.yandex.javacourse.schedule.tasks.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEADER = "id,type,name,status,description,startTime,duration,epic";

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.size() < 2) {
                return manager;
            }
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                Task task = taskFromString(line);
                if (task == null) {
                    continue;
                }

                switch (getTaskType(line)) {
                    case TASK -> manager.tasks.put(task.getId(), task);
                    case EPIC -> manager.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                    }
                }
                if (task.getStartTime() != null) {
                    manager.prioritizedTasks.add(task);
                }

                if (task.getId() > manager.getGeneratorId()) {
                    manager.setGeneratorId(task.getId());
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicTime(epic);
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при загрузке из файла " + file.getName(), e);
        }
        return manager;
    }

    private static TaskType getTaskType(String line) {
        String[] parts = line.split(",");
        return TaskType.valueOf(parts[1]);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    private static String taskToString(Task task) {
        String type;
        String epicField = "";

        if (task instanceof Epic) {
            type = TaskType.EPIC.name();
        } else if (task instanceof Subtask subtask) {
            type = TaskType.SUBTASK.name();
            epicField = String.valueOf(subtask.getEpicId());
        } else {
            type = TaskType.TASK.name();
        }
        String startTime = "";
        if (task.getStartTime() != null) {
            startTime = task.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        String durationMinutes = "";
        if (task.getDuration() != null) {
            durationMinutes = task.getDuration().toMinutes() + "";
        }

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                             task.getId(),
                             type,
                             escapeCsv(task.getName()),
                             task.getStatus(),
                             escapeCsv(task.getDescription()),
                             startTime,
                             durationMinutes,
                             epicField
        );
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null || line.isEmpty()) return result;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private static Task taskFromString(String value) {
        if (value == null || value.isEmpty()) return null;
        List<String> parts = parseCsvLine(value);
        int id = Integer.parseInt(parts.get(0));
        TaskType type = TaskType.valueOf(parts.get(1));
        String name = parts.get(2);
        TaskStatus status = TaskStatus.valueOf(parts.get(3));
        String description = parts.get(4);
        LocalDateTime startTime = parts.get(5).isEmpty()
                ? null
                : LocalDateTime.parse(parts.get(5), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Duration duration = parts.get(6).isEmpty()
                ? null
                : Duration.ofMinutes(Long.parseLong(parts.get(6)));

        return switch (type) {
            case TASK -> {
                Task task = new Task(id, name, description, status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                yield task;
            }
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> {
                Subtask subtask = new Subtask(id, name, description, status, Integer.parseInt(parts.get(7)));
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                yield subtask;
            }
        };
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    protected void save() {
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER + System.lineSeparator());

            for (Task task : getTasks()) {
                writer.write(taskToString(task) + System.lineSeparator());
            }

            for (Epic epic : getEpics()) {
                writer.write(taskToString(epic) + System.lineSeparator());
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(taskToString(subtask) + System.lineSeparator());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении задач в файл " + file.getName(), e);
        }
    }

}
