package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleGet(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);

        if (idFromPath.isEmpty()) {
            sendResponse(exchange, manager.getSubtasks());
            return;
        }
        int id = idFromPath.get();
        Task task = manager.getSubtask(id);
        if (task == null) {
            sendNotFound(exchange, "SubTask " + id + " not found");
        } else {
            sendResponse(exchange, task);
        }
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);
        Subtask bodySubTask = getBody(exchange, Subtask.class);

        if (idFromPath.isPresent()) {
            manager.updateSubtask(bodySubTask);
            sendCreated(exchange, "SubTask Updated");
        } else {
            manager.addNewSubtask(bodySubTask);
            sendCreated(exchange, "SubTask Created");
        }
    }

    @Override
    public void handleDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);
        if (idFromPath.isEmpty()) {
            sendBadRequest(exchange, "Id is not specified");
            return;
        }

        manager.deleteSubtask(idFromPath.get());
        sendText(exchange, "Subtask Deleted");
    }
}
