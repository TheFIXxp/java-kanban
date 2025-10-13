package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).thenComparingInt(Task::getId));
    private int generatorId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

    @Override
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public int getGeneratorId() {
        return generatorId;
    }

    public void setGeneratorId(int generatorId) {
        this.generatorId = generatorId;
    }

    @Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public List<Subtask> getEpicSubtasks(int epicId) {
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toList());
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		historyManager.addTask(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		historyManager.addTask(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		historyManager.addTask(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
        if (hasIntersections(task)) {
            throw new IllegalArgumentException("Task cannot be added because of intersections");
        }
		final int id = ++generatorId;
		task.setId(id);
		tasks.put(id, task);
        addToPrioritized(task);
        return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
        if (hasIntersections(epic)) {
            throw new IllegalArgumentException("Epic cannot be added because of intersections");
        }
		final int id = ++generatorId;
		epic.setId(id);
		epics.put(id, epic);
        addToPrioritized(epic);
        return id;
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException("Subtask cannot be added because of intersections");
        }
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		final int id = ++generatorId;
		subtask.setId(id);
		subtasks.put(id, subtask);
        addToPrioritized(subtask);
        epic.addSubtaskId(subtask.getId());
		updateEpicStatus(epicId);
        removeFromPrioritized(epic);
        updateEpicTime(epic);
        addToPrioritized(epic);
        return id;
	}

	@Override
	public void updateTask(Task task) {
		final int id = task.getId();
		final Task savedTask = tasks.get(id);
		if (savedTask == null) {
			return;
		}
        removeFromPrioritized(task);
		tasks.put(id, task);
        addToPrioritized(task);
    }

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		savedEpic.setName(epic.getName());
		savedEpic.setDescription(epic.getDescription());
        removeFromPrioritized(savedEpic);
        updateEpicTime(epic.getId());
        addToPrioritized(savedEpic);
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		final int id = subtask.getId();
		final int epicId = subtask.getEpicId();
		final Subtask savedSubtask = subtasks.get(id);
		if (savedSubtask == null) {
			return;
		}
        removeFromPrioritized(savedSubtask);
		final Epic epic = epics.get(epicId);
		if (epic == null) {
			return;
		}
		subtasks.put(id, subtask);
        addToPrioritized(subtask);
		updateEpicStatus(epicId);
        removeFromPrioritized(epic);
        updateEpicTime(epic);
        addToPrioritized(epic);
    }

	@Override
	public void deleteTask(int id) {
        Task remove = tasks.remove(id);
        removeFromPrioritized(remove);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
        historyManager.remove(id);
        removeFromPrioritized(epic);
		for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
            removeFromPrioritized(subtask);
		}
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
		if (subtask == null) {
			return;
		}
		Epic epic = epics.get(subtask.getEpicId());
        historyManager.remove(id);
		epic.removeSubtask(id);
        removeFromPrioritized(subtask);
		updateEpicStatus(epic.getId());
        removeFromPrioritized(epic);
        updateEpicTime(epic);
        addToPrioritized(epic);
	}

	@Override
	public void deleteTasks() {
        for (Task task : tasks.values()) {
            removeFromPrioritized(task);
        }
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Epic epic : epics.values()) {
			epic.cleanSubtaskIds();
			updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
		}
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
        for (Epic epic : epics.values()) {
            removeFromPrioritized(epic);
        }
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritized(subtask);
        }
        epics.clear();
        subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	private void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}

    public void updateEpicTime(Epic epic) {
        List<Integer> subs = epic.getSubtaskIds();

        if (subs.isEmpty()) {
            return;
        }

        List<Subtask> subTaskList = subs.stream()
                .map(subtasks::get)
                .toList();

        Duration subTasksDuration = subTaskList.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        LocalDateTime subTasksStartTime = subTaskList.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime subTasksEndTime = subTaskList.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);


        epic.setDuration(subTasksDuration);
        epic.setStartTime(subTasksStartTime);
        epic.setEndTime(subTasksEndTime);
    }

    public void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        updateEpicTime(epic);
    }

    private void removeFromPrioritized(Task task) {
        removeFromPrioritizedById(task.getId());
    }

    private void removeFromPrioritizedById(int id) {
        prioritizedTasks.removeIf(t -> t.getId() == id);
    }

    public void addToPrioritized(Task t) {
        if (t != null && t.getStartTime() != null) {
            prioritizedTasks.add(t);
        }
    }

    private boolean hasIntersections(Task task) {
        return prioritizedTasks.stream().anyMatch(task1 -> task1.isIntersects(task));
    }

}
