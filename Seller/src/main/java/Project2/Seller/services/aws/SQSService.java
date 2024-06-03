package Project2.Seller.services.aws;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
public class SQSService {

    @Autowired
    private SqsClient sqsClient;


    public String createQueue(String name){
        CreateQueueRequest request = CreateQueueRequest.builder()
                .queueName(name)
                .build();

        CreateQueueResponse response = sqsClient.createQueue(request);
        return response.queueUrl();
    }

    public List<Message> getMessages(String queueUrl){
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .build();
        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        return response.messages();
    }

    public SendMessageResponse sendWinningMessage(String queueUrl, String auctionId, Integer bidAmount){
        String body = "{\n" +
                "\"auctionId\": \"" + auctionId + "\",\n" +
                "\"amountOfBid\": " + bidAmount + "\n" +
                "}";
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build();

        SendMessageResponse response = sqsClient.sendMessage(request);
        return response;
    }
    public void deleteQueue(String queueUrl){
        DeleteQueueRequest request = DeleteQueueRequest.builder()
                .queueUrl(queueUrl)
                .build();
        sqsClient.deleteQueue(request);
    }

}
