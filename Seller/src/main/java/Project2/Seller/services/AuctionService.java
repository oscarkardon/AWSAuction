package Project2.Seller.services;

import Project2.Seller.models.AuctionRequest;
import Project2.Seller.models.AuctionRequestResponse;
import Project2.Seller.models.BidMessage;
import Project2.Seller.services.aws.SNSService;
import Project2.Seller.services.aws.SQSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuctionService {
    private SQSService sqsService;
    private SNSService snsService;
    private String forSaleArn;
    @Autowired
    public AuctionService(SQSService sqsService, SNSService snsService, String forSaleArn) {
        this.sqsService = sqsService;
        this.snsService = snsService;
        this.forSaleArn = forSaleArn;
    }


    public AuctionRequestResponse processAuctionRequest(AuctionRequest auctionRequest) {
        AuctionRequestResponse auctionRequestResponse = new AuctionRequestResponse();
        auctionRequestResponse.setAuctionId(UUID.randomUUID().toString());
        auctionRequestResponse.setProductCategory(auctionRequest.getProductCategory());
        auctionRequestResponse.setAuctionLength(auctionRequest.getAuctionLength());

        auctionRequestResponse.setQueue(sqsService.createQueue(auctionRequest.getProductTitle()));

        PublishResponse response = snsService.postForSale(auctionRequest.getProductTitle(), auctionRequest.getDescription(), auctionRequestResponse.getAuctionId(), auctionRequestResponse.getProductCategory(), auctionRequestResponse.getAuctionLength(), auctionRequestResponse.getQueue(), forSaleArn);
        System.out.println("Published Auction Id " + auctionRequestResponse.getAuctionId());
        System.out.println("      " + auctionRequest.getProductTitle() + " " + auctionRequest.getDescription());
        System.out.println("     in category " + auctionRequestResponse.getProductCategory());
        try {
            System.out.println("About to sleep " + auctionRequestResponse.getAuctionLength());
            TimeUnit.SECONDS.sleep(auctionRequestResponse.getAuctionLength());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        endAuction(auctionRequestResponse.getQueue(), auctionRequestResponse.getAuctionId());
        return auctionRequestResponse;
    }

    public void endAuction(String queueUrl, String auctionId) {
        System.out.println("Woke up to process auction " + auctionId);
        List<Message> messages = sqsService.getMessages(queueUrl);
        if(!messages.isEmpty()) {
            BidMessage winner = getWinner(messages);
            System.out.println("The winning bid for " + auctionId + " was " + winner.getEmail() + " for " + winner.getBidAmount());

            sqsService.sendWinningMessage(winner.getAcceptanceQueueURL(), auctionId, winner.getBidAmount());
            snsService.endAuctionMessage(winner.getBidAmount(), winner.getAcceptanceQueueURL());
        }
        else{
            snsService.endAuctionMessage(0, auctionId);
            System.out.println("No bid received for auction " + auctionId);
        }
        System.out.println("Closing auction " + auctionId);
        sqsService.deleteQueue(queueUrl);
    }

    public BidMessage getWinner(List<Message> messages) {
        ObjectMapper objectMapper = new ObjectMapper();
        BidMessage winner = null;
        try {
            winner = objectMapper.readValue(messages.get(0).body(), BidMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (Message message : messages) {
            if (message != null) {
                try {
                    BidMessage bidMessage = objectMapper.readValue(message.body(), BidMessage.class);
                    Integer messageBidAmount = bidMessage.getBidAmount();
                    Integer winnerBidAmount = winner.getBidAmount();
                    if (winnerBidAmount < messageBidAmount) {
                         winner = bidMessage;
                    }
                    return winner;
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
        return winner;
    }

    private Integer getBidAmount(Message message) {
        if (message != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                BidMessage bidMessage = objectMapper.readValue(message.body(), BidMessage.class);
                Integer messageBidAmount = bidMessage.getBidAmount();
                return bidMessage.getBidAmount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private String getQueueUrl(Message message){
        if (message != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                BidMessage bidMessage = objectMapper.readValue(message.body(), BidMessage.class);
                Integer messageBidAmount = bidMessage.getBidAmount();
                return bidMessage.getAcceptanceQueueURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
