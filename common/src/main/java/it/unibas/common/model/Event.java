package it.unibas.common.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Event implements Cloneable {

    private LocalDateTime timestamp;

    @NonNull
    private EventType type;

    @NonNull
    private EventGroup group;

    @NonNull
    private String userId;

    @Builder.Default
    private EventSeverity severity = EventSeverity.LOW;


    @Override
    public Event clone() {
        try {
            return (Event) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

}
