package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.http.GsonConstant;
import ru.yandex.javacourse.schedule.http.StatusCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    public static final String JSON_MIME_TYPE = "application/json;charset=utf-8";

    protected void sendResponse(HttpExchange exchange, StatusCode statusCode, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_MIME_TYPE);
        exchange.sendResponseHeaders(statusCode.getCode(), resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendResponse(HttpExchange exchange, Object object) throws IOException {
        String json = GsonConstant.GSON.toJson(object);
        sendResponse(exchange, StatusCode.OK, json);
    }

    protected void sendText(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, StatusCode.OK, message);
    }

    protected void sendCreated(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, StatusCode.CREATED, message);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, StatusCode.NOT_FOUND, message);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, StatusCode.BAD_REQUEST, message);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, StatusCode.NOT_ACCEPTABLE, message);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, StatusCode.METHOD_NOT_ALLOWED, message);
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
            sendResponse(exchange, StatusCode.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    public abstract void handleGet(HttpExchange exchange) throws IOException;

    public abstract void handlePost(HttpExchange exchange) throws IOException;

    public abstract void handleDelete(HttpExchange exchange) throws IOException;


}
