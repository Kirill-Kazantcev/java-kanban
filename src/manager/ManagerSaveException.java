package manager;

/**
 * Исключение, выбрасываемое при ошибках сохранения или загрузки состояния менеджера в/из файла.
 * <p>
 * Является непроверяемым исключением (наследует RuntimeException), что позволяет не изменять
 * сигнатуры методов интерфейса TaskManager при добавлении функциональности автосохранения.
 * <p>
 * Возможные причины возникновения:
 * <ul>
 *   <li>Ошибка ввода-вывода при записи в файл</li>
 *   <li>Ошибка ввода-вывода при чтении из файла</li>
 *   <li>Недостаточно прав для доступа к файлу</li>
 *   <li>Отсутствие директории для сохранения файла</li>
 *   <li>Некорректный формат данных в файле при загрузке</li>
 * </ul>
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 6
 */
public class ManagerSaveException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   исходная причина ошибки (обычно IOException)
     */
    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}