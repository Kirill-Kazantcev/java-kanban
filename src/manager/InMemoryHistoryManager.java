package manager;

import tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация менеджера истории просмотров на основе собственного двусвязного списка и HashMap.
 * Обеспечивает константное время выполнения операций добавления, удаления и поиска задач в истории.
 * История не имеет ограничения по размеру и не содержит дубликатов задач.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 6
 */
public class InMemoryHistoryManager implements HistoryManager {

    /**
     * Узел двусвязного списка для хранения задачи и ссылок на соседние узлы.
     */
    private static class Node {
        /** Хранимая задача */
        Task task;
        /** Ссылка на следующий узел */
        Node next;
        /** Ссылка на предыдущий узел */
        Node prev;

        /**
         * Конструктор узла двусвязного списка.
         *
         * @param prev предыдущий узел
         * @param task задача для хранения
         * @param next следующий узел
         */
        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    /** Хэш-таблица для быстрого доступа к узлам по ID задачи */
    private final Map<Integer, Node> historyMap = new HashMap<>();

    /** Ссылка на первый узел списка */
    private Node first;

    /** Ссылка на последний узел списка */
    private Node last;

    /**
     * Добавляет задачу в конец двусвязного списка.
     * Время выполнения - O(1).
     *
     * @param task задача для добавления
     */
    private void linkLast(Task task) {
        final Node oldLast = last;
        final Node newNode = new Node(oldLast, task, null);
        last = newNode;

        if (oldLast == null) {
            first = newNode;
        } else {
            oldLast.next = newNode;
        }
    }

    /**
     * Удаляет указанный узел из двусвязного списка.
     * Время выполнения - O(1).
     *
     * @param node узел для удаления
     */
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        Node prevNode = node.prev;
        Node nextNode = node.next;

        if (prevNode == null) {
            first = nextNode;
        } else {
            prevNode.next = nextNode;
            node.prev = null;
        }

        if (nextNode == null) {
            last = prevNode;
        } else {
            nextNode.prev = prevNode;
            node.next = null;
        }

        node.task = null;
    }

    /**
     * Добавляет задачу в историю просмотров.
     * Если задача уже есть в истории, она удаляется и добавляется в конец.
     * Время выполнения - O(1).
     *
     * @param task задача для добавления
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());
        linkLast(task);
        historyMap.put(task.getId(), last);
    }

    /**
     * Удаляет задачу из истории просмотров по идентификатору.
     * Время выполнения - O(1).
     *
     * @param id идентификатор задачи для удаления
     */
    @Override
    public void remove(int id) {
        Node nodeToRemove = historyMap.remove(id);
        if (nodeToRemove != null) {
            removeNode(nodeToRemove);
        }
    }

    /**
     * Полностью очищает историю просмотров.
     * Время выполнения - O(1).
     */
    @Override
    public void clear() {
        historyMap.clear();
        first = null;
        last = null;
    }

    /**
     * Возвращает список просмотренных задач в порядке их просмотра.
     * Время выполнения - O(n), где n - размер истории.
     *
     * @return список задач в порядке просмотра
     */
    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node current = first;

        while (current != null) {
            historyList.add(current.task);
            current = current.next;
        }

        return historyList;
    }
}