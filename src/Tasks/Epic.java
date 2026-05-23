package Tasks;

import Tools.TaskStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс эпика - сложной задачи, состоящей из подзадач.
 * Наследуется от Task. Статус эпика рассчитывается автоматически
 * на основе статусов всех его подзадач.
 *
 * @author Kirill-Kazantcev
 * @version 3.0
 * @since Sprint 4
 */
public class Epic extends Task {

    /** Список идентификаторов подзадач, входящих в эпик */
    private final List<Integer> subtaskIds;

    /**
     * Конструктор для создания нового эпика.
     * Статус автоматически устанавливается в NEW.
     *
     * @param title название эпика
     * @param description описание эпика
     */
    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    /**
     * Возвращает копию списка идентификаторов подзадач.
     *
     * @return список ID подзадач
     */
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    /**
     * Добавляет идентификатор подзадачи в эпик.
     *
     * @param subtaskId идентификатор подзадачи
     */
    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    /**
     * Удаляет идентификатор подзадачи из эпика.
     *
     * @param subtaskId идентификатор подзадачи
     */
    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    /**
     * Очищает список подзадач эпика.
     */
    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    /**
     * Возвращает строковое представление эпика.
     * Включает список идентификаторов подзадач.
     *
     * @return строковое представление эпика
     */
    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}