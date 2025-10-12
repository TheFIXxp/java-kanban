package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
	protected int id;
	protected String name;
	protected TaskStatus status;
	protected String description;
    protected Duration duration;
    protected LocalDateTime startTime;

	public Task(int id, String name, String description, TaskStatus status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public Task(String name, String description, TaskStatus status) {
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return id == task.id;
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status='" + status + '\'' +
				", description='" + description + '\'' +
				'}';
	}

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public boolean isIntersects(Task task) {
        if (task == null || getStartTime() == null || getEndTime() == null || task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }
        return getEndTime().isAfter(task.getStartTime()) && getStartTime().isBefore(task.getEndTime());
    }

}
