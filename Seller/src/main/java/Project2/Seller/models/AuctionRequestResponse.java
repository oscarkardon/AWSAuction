package Project2.Seller.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuctionRequestResponse {
    private String auctionId;
//    private String productTitle;
    private String productCategory;
//    private String description;
    private Integer auctionLength;
    private String queue; //could be bidQueueUrl
//    private String notificationId;
}
