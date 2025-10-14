package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, manager.getPrioritizedTasks());
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "Unsupported request method: POST");
    }

    @Override
    public void handleDelete(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "Unsupported request method: POST");
    }
}
