package Tasks;

import Tools.TaskStatus;
import java.util.Objects;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Переопределение методов из Object
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // проверка на тот же объект
        if (o == null || getClass() != o.getClass()) return false; // сверка типа
        Task task = (Task) o;
        return id == task.id; // сверка id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // хэш по id
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}