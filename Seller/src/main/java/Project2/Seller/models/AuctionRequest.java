package Project2.Seller.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AuctionRequest {
    private String productTitle;
    private String productCategory;
    private String description;
    private Integer auctionLength;
}
