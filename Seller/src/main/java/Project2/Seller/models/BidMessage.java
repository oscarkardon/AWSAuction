package Project2.Seller.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BidMessage {
    private String email;
    private Integer bidAmount;
    private String acceptanceQueueURL;
}

