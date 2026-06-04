package server.exceptions;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему ресурсу.
 * Соответствует HTTP статусу 404 Not Found.
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}