package Project2.Bidder.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class NotificationMessageAttribute {
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Value")
    private String value;
}