package it.unibas.common.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(of = "id")
public abstract class Event implements Cloneable {
//    @NonNull
//    private String id;

    private LocalDateTime timestamp;

    @NonNull
    private EventType type;

    @NonNull
    private String userId;

    @Builder.Default
    private EventSeverity severity = EventSeverity.LOW;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Override
    public Event clone() {
        try {
            Event cloned = (Event) super.clone();
            // Deep copy della metadata map
            cloned.metadata = new HashMap<>(this.metadata);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

    // Helper methods per metadata
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    public Map<String, Object> getMetadata() {
        return this.metadata != null ? new HashMap<>(this.metadata) : new HashMap<>();
    }
}
