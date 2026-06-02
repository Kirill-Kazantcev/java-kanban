package tasks;

import tools.TaskStatus;
import tools.TaskType;
import java.time.Duration;
import java.time.LocalDateTime;
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

    /** Время завершения эпика (расчётное) */
    private LocalDateTime endTime;

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
        this.setDuration(Duration.ZERO);
        this.setStartTime(null);
        this.endTime = null;
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
     * Пересчитывает временные параметры эпика на основе подзадач.
     *
     * @param subtasks список подзадач эпика
     */
    public void recalculateTimes(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            setDuration(Duration.ZERO);
            setStartTime(null);
            this.endTime = null;
            return;
        }
        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        for (Subtask subtask : subtasks) {
            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
            LocalDateTime subStart = subtask.getStartTime();
            if (subStart != null) {
                if (earliestStart == null || subStart.isBefore(earliestStart)) {
                    earliestStart = subStart;
                }
                LocalDateTime subEnd = subtask.getEndTime();
                if (subEnd != null && (latestEnd == null || subEnd.isAfter(latestEnd))) {
                    latestEnd = subEnd;
                }
            }
        }
        setDuration(totalDuration);
        setStartTime(earliestStart);
        this.endTime = latestEnd;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Возвращает тип задачи.
     * Для эпика возвращает EPIC.
     *
     * @return тип задачи (EPIC)
     */
    @Override
    public TaskType getType() {
        return TaskType.EPIC;
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
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : 0) +
                ", startTime=" + getStartTime() +
                ", endTime=" + endTime +
                '}';
    }
}