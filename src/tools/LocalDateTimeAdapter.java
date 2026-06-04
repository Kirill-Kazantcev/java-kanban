package tools;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Адаптер Gson для сериализации/десериализации LocalDateTime.
 * Использует формат ISO_LOCAL_DATE_TIME (например, "2026-06-05T10:00:00").
 * <p>
 * Пример JSON: {"startTime": "2026-06-05T10:00:00"}
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Сериализует LocalDateTime в JSON строку в формате ISO.
     *
     * @param out   JsonWriter для записи
     * @param value значение LocalDateTime (может быть null)
     * @throws IOException при ошибке записи
     */
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(formatter));
        }
    }

    /**
     * Десериализует JSON строку в LocalDateTime.
     *
     * @param in JsonReader для чтения
     * @return LocalDateTime из строки ISO, или null если значение отсутствует
     * @throws IOException при ошибке чтения
     */
    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        try {
            String value = in.nextString();
            if (value == null || value.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(value, formatter);
        } catch (Exception e) {
            in.skipValue();
            return null;
        }
    }
}