package Manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Метод getDefault() должен возвращать проинициализированный TaskManager");

        assertNotNull(taskManager.getTasks());
        assertNotNull(taskManager.getEpics());
        assertNotNull(taskManager.getSubtasks());
        assertNotNull(taskManager.getHistory());
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Метод getDefaultHistory() должен возвращать проинициализированный HistoryManager");

        assertNotNull(historyManager.getHistory());
    }
}