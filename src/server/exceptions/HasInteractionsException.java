package server.exceptions;

/**
 * Исключение, выбрасываемое при попытке создать или обновить задачу,
 * которая пересекается по времени с существующими задачами.
 * Соответствует HTTP статусу 406 Not Acceptable.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class HasInteractionsException extends RuntimeException {

    public HasInteractionsException(String message) {
        super(message);
    }

    public HasInteractionsException(String message, Throwable cause) {
        super(message, cause);
    }
}