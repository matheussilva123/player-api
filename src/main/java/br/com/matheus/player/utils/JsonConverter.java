package br.com.matheus.player.utils;

import br.com.matheus.player.exception.FileConverterException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JsonConverter {

    private final ObjectMapper objectMapper;

    public JsonConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new FileConverterException(String.format("Failed to converter object to json: %s", e.getMessage()));
        }
    }

    public <T> List<T> toList(final String json, final Class<? extends T> clazz) {
        try {
            if (json == null || json.isBlank() || json.isEmpty() || json.contains("[]")) {
                return Collections.emptyList();
            }
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (final JsonProcessingException e) {
            throw new FileConverterException(String.format("Failed to converter object: %s", e.getMessage()));
        }
    }
}
