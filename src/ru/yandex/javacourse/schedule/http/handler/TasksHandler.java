package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleGet(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);

        if (idFromPath.isEmpty()) {
            sendResponse(exchange, manager.getTasks());
            return;
        }

        int id = idFromPath.get();
        Task task = manager.getTask(id);
        if (task == null) {
            sendNotFound(exchange, "Task " + id + " not found");
        } else {
            sendResponse(exchange, task);
        }
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);
        Task bodyTask = getBody(exchange, Task.class);

        if (idFromPath.isPresent()) {
            manager.updateTask(bodyTask);
            sendCreated(exchange, "Task Updated");
        } else {
            manager.addNewTask(bodyTask);
            sendCreated(exchange, "Task Created");
        }
    }

    @Override
    public void handleDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);
        if (idFromPath.isEmpty()) {
            sendBadRequest(exchange, "Id is not specified");
            return;
        }

        manager.deleteTask(idFromPath.get());
        sendText(exchange, "Task Deleted");
    }
}
