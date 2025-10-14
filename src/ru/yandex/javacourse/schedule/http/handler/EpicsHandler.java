package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.util.Optional;


public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleGet(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);

        if (idFromPath.isEmpty()) {
            sendResponse(exchange, manager.getEpics());
            return;
        }

        int id = idFromPath.get();
        Task task = manager.getEpic(id);

        if (task == null) {
            sendNotFound(exchange, "Epic " + id + " not found");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.contains("subtasks")) {
            sendResponse(exchange, manager.getEpicSubtasks(id));
        } else {
            sendResponse(exchange, task);
        }
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        Epic bodyTask = getBody(exchange, Epic.class);
        manager.addNewEpic(bodyTask);
        sendCreated(exchange, "Epic Created");
    }

    @Override
    public void handleDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> idFromPath = getIdFromPath(exchange);
        if (idFromPath.isEmpty()) {
            sendBadRequest(exchange, "Id is not specified");
            return;
        }

        manager.deleteEpic(idFromPath.get());
        sendText(exchange, "Epic Deleted");
    }
}
