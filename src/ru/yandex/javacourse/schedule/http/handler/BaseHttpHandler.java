package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.http.GsonConstant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    public static final String JSON_MIME_TYPE = "application/json;charset=utf-8";

    protected void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_MIME_TYPE);
        exchange.sendResponseHeaders(statusCode, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendResponse(HttpExchange exchange, Object object) throws IOException {
        String json = GsonConstant.GSON.toJson(object);
        sendResponse(exchange, 200, json);
    }

    protected void sendText(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 200, message);
    }

    protected void sendCreated(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 201, message);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 404, message);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 400, message);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 406, message);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 405, message);
    }

    protected <T> T getBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        return GsonConstant.GSON.fromJson(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), clazz);
    }


    protected Optional<Integer> getIdFromPath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        try {
            if (parts.length > 2) {
                return Optional.of(Integer.parseInt(parts[2]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        try {
            switch (requestMethod) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendMethodNotAllowed(exchange, "Unsupported request method: " + requestMethod);
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    public abstract void handleGet(HttpExchange exchange) throws IOException;

    public abstract void handlePost(HttpExchange exchange) throws IOException;

    public abstract void handleDelete(HttpExchange exchange) throws IOException;
}
