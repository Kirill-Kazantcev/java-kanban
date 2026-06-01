package tools;

/**
 * Перечисление типов задач в системе.
 * Используется для идентификации типа задачи при сериализации/десериализации в CSV формат.
 * <p>
 * Возможные типы задач:
 * <ul>
 *   <li>TASK - обычная задача</li>
 *   <li>EPIC - эпик (большая задача, содержащая подзадачи)</li>
 *   <li>SUBTASK - подзадача, принадлежащая эпику</li>
 * </ul>
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 6
 */
public enum TaskType {
    TASK,
    EPIC,
    SUBTASK
}