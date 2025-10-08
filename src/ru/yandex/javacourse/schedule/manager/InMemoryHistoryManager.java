package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> history = new HashMap<>();
    private Node first;
    private Node last;

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();

        for (Node x = first; x != null; x = x.next) {
            historyList.add(x.item);
        }
        return historyList;
    }

    @Override
    public void addTask(Task task) {
        if (task == null) {
            return;
        }

        final int id = task.getId();
        Node node = history.remove(id);
        if (node != null) {
            removeNode(node);
        }

        history.put(id, linkLast(task));
    }

    @Override
    public void remove(int id) {
        Node remove = history.remove(id);
        if (remove != null) {
            removeNode(remove);
        }
    }

    private void removeNode(Node node) {
        final Node next = node.next;
        final Node prev = node.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.item = null;
    }

    private Node linkLast(Task e) {
        final Node l = last;
        final Node newNode = new Node(l, e, null);
        last = newNode;
        if (l == null) first = newNode;
        else l.next = newNode;
        return newNode;
    }

    private static class Node {
        Task item;
        Node next;
        Node prev;

        Node(Node prev, Task element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
