package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, taskManager.getHistory());
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "Unsupported request method: POST");
    }

    @Override
    public void handleDelete(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange, "Unsupported request method: DELETE");
    }
}
