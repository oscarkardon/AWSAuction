package Project2.Bidder.services;

import Project2.Bidder.models.NotificationMessage;
import Project2.Bidder.models.NotificationMessageAttribute;
import Project2.Bidder.models.WinningBidMessage;
import Project2.Bidder.services.aws.SQSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.*;

@Service
public class BidderService {
    @Autowired
    private SQSService sqsService;
    @Value("${forSaleArn}")
    private String forSaleArn;

    @Value("${auctionCompleteArn}")
    private String auctionCompleteArn;

    @Value("${bidderName}")
    private String bidderName;

    @Value("${bidderEmail}")
    private String bidderEmail;
    @Value("${accoutId}")
    private String accountId;


    private String subscriptionQueueUrl;
    private String auctionCompleteQueueUrl;
    private String acceptedQueueUrl;
    private Integer balance;
    private Integer bidAmount;
    @PostConstruct
    public void initialize(){
        System.out.println("Starting Bidder Process for " + bidderName);
        System.out.println("Waiting for Bids");
        balance = 100;
        createQueues();
        //ChatGPT helped build the Thread
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            every20Seconds();
        }, 0, 20, TimeUnit.SECONDS);

    }
    public void createQueues(){
        subscriptionQueueUrl = sqsService.createQueue(bidderName + "_subscription_queue");
        String queueName = subscriptionQueueUrl.substring(subscriptionQueueUrl.lastIndexOf("/") + 1);
        String subscriptionQueueArn = "arn:aws:sqs:us-east-1:" + accountId + ":" + queueName;

        sqsService.setPermissions(subscriptionQueueArn, forSaleArn, subscriptionQueueUrl);
        String subscriptionArn = sqsService.subscribeQueueToSNS(forSaleArn, subscriptionQueueArn);
        sqsService.setFilter(subscriptionArn);

        auctionCompleteQueueUrl = sqsService.createQueue(bidderName + "_auction_complete_queue");
        String auctionCompleteQueueName = auctionCompleteQueueUrl.substring(auctionCompleteQueueUrl.lastIndexOf("/") + 1);
        String auctionCompleteQueueArn = "arn:aws:sqs:us-east-1:" + accountId + ":" + auctionCompleteQueueName;
        sqsService.setPermissions(auctionCompleteQueueArn, auctionCompleteArn, auctionCompleteQueueUrl);
        sqsService.subscribeQueueToSNS(auctionCompleteArn, auctionCompleteQueueArn);

        acceptedQueueUrl = sqsService.createQueue(bidderName + "_accepted_bids_queue");
    }
    public void every20Seconds(){
        if(balance > 0) {
            newBids();
            auctionComplete();
            acceptedBids();
        }
        else {
            System.out.println("Bidder is out of money");
        }
    }

public void newBids(){
    System.out.println("Looking for items to bid on");
    List<Message> messages = sqsService.getMessages(subscriptionQueueUrl);
    if(messages.isEmpty()){
        System.out.println("Nothing available to bid on");
    }
    for (Message message : messages) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            NotificationMessage notificationMessage = objectMapper.readValue(message.body(), NotificationMessage.class);
            Map<String, NotificationMessageAttribute> attributeMap = notificationMessage.getMessageAttributes();
            String queueUrl = attributeMap.get("queue").getValue();
            String auctionId =  attributeMap.get("auctionId").getValue();
            String email = bidderEmail;
            String acceptanceQueueURL = acceptedQueueUrl;
            Random random = new Random();
            bidAmount = random.nextInt(balance) + 1;
            balance -= bidAmount;
            sqsService.bid(queueUrl, bidAmount, email, acceptanceQueueURL);
            System.out.println("Submitted bid for Auction Id " +  auctionId + " The bid was " + bidAmount + " for" + notificationMessage.getMessage());
            sqsService.clearQueue(subscriptionQueueUrl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

    public void acceptedBids(){
        List<Message> messages = sqsService.getMessages(acceptedQueueUrl);
        for (Message message : messages) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                WinningBidMessage notificationMessage = objectMapper.readValue(message.body(), WinningBidMessage.class);
                String auctionId = notificationMessage.getAuctionId();
                double bidAmount = notificationMessage.getAmountOfBid();
                System.out.println("Accepted Bid for " + auctionId + " with a bid of " + bidAmount);
                balance -= (int) bidAmount;
                System.out.println("Account balance is now " + balance);
                sqsService.clearQueue(acceptedQueueUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public void auctionComplete(){
        List<Message> messages = sqsService.getMessages(auctionCompleteQueueUrl);
        for (Message message : messages) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                NotificationMessage notificationMessage = objectMapper.readValue(message.body(), NotificationMessage.class);
                Map<String, NotificationMessageAttribute> attributeMap = notificationMessage.getMessageAttributes();
                String auctionId = attributeMap.get("auctionId").getValue();
                System.out.println("Auction Complete " + auctionId);
                balance += bidAmount;
                sqsService.clearQueue(auctionCompleteQueueUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
