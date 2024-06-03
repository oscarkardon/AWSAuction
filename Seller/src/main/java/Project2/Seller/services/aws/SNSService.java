package Project2.Seller.services.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class SNSService {

    @Autowired
    private String auctionCompleteArn;
    @Autowired
    private SnsClient snsClient;


    public PublishResponse postForSale(String subject, String message, String auctionId, String productCategory, Integer length, String queue, String topicArn){
        //ChatGPT helped with the messageAttributes mapping
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("auctionId", MessageAttributeValue.builder().stringValue(auctionId).dataType("String").build());
        messageAttributes.put("category", MessageAttributeValue.builder().stringValue(productCategory).dataType("String").build());
        messageAttributes.put("length", MessageAttributeValue.builder().stringValue(Integer.toString(length)).dataType("Number").build());
        messageAttributes.put("queue", MessageAttributeValue.builder().stringValue(queue).dataType("String").build());

        PublishRequest request = PublishRequest.builder()
                .subject(subject)
                .message(message)
                .messageAttributes(messageAttributes)
                .topicArn(topicArn)
                .build();

        PublishResponse response = snsClient.publish(request);
        System.out.println("Should have pushed a message");
        return response;
    }

    public PublishResponse endAuctionMessage(Integer winningBidAmount, String auctionId){
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("auctionId", MessageAttributeValue.builder().stringValue(auctionId).dataType("String").build());

        PublishRequest request = PublishRequest.builder()
                .subject("Auction Complete")
                .message("Sold for " + winningBidAmount)
                .messageAttributes(messageAttributes)
                .topicArn(auctionCompleteArn)
                .build();

        PublishResponse response = snsClient.publish(request);
        return response;
    }
}
