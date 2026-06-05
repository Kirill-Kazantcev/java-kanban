package tools;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

/**
 * Адаптер Gson для сериализации/десериализации Duration.
 * Преобразует Duration в количество минут при сериализации и обратно.
 * <p>
 * Пример JSON: {"duration": 60} (60 минут)
 *
 * @author Kirill-Kazantcev
 * @version 1.0
 * @since Sprint 9
 */
public class DurationAdapter extends TypeAdapter<Duration> {

    /**
     * Сериализует Duration в JSON число (количество минут).
     *
     * @param out   JsonWriter для записи
     * @param value значение Duration (может быть null)
     * @throws IOException при ошибке записи
     */
    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toMinutes());
        }
    }

    /**
     * Десериализует JSON число в Duration.
     *
     * @param in JsonReader для чтения
     * @return Duration из количества минут, или null если значение отсутствует
     * @throws IOException при ошибке чтения
     */
    @Override
    public Duration read(JsonReader in) throws IOException {
        try {
            long minutes = in.nextLong();
            return Duration.ofMinutes(minutes);
        } catch (Exception e) {
            in.skipValue();
            return Duration.ZERO;
        }
    }
}