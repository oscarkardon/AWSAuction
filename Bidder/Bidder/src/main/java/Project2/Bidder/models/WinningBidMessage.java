package Project2.Bidder.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WinningBidMessage {
    @JsonProperty("auctionId")
    private String auctionId;
    @JsonProperty("amountOfBid")
    private double amountOfBid;
}
