package ru.yandex.javacourse.schedule.http;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacourse.schedule.http.handler.*;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager, int port) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        registerContexts(manager);
    }

    private void registerContexts(TaskManager manager) {
        httpServer.createContext("/tasks", new TasksHandler(manager));
        httpServer.createContext("/subtasks", new SubtasksHandler(manager));
        httpServer.createContext("/epics", new EpicsHandler(manager));
        httpServer.createContext("/history", new HistoryHandler(manager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

}

