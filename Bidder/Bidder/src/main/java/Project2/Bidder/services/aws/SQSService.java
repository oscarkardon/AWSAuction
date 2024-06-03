package Project2.Bidder.services.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQSService {

    private SqsClient sqsClient;
    private SnsClient snsClient;
    @Autowired
    public SQSService(SqsClient sqsClient, SnsClient snsClient) {
        this.sqsClient = sqsClient;
        this.snsClient = snsClient;
    }
    @Value("${categories}")
    private String categories;

    public String createQueue(String name){
        CreateQueueRequest request = CreateQueueRequest.builder()
                .queueName(name)
                .build();
        CreateQueueResponse response = sqsClient.createQueue(request);
        return response.queueUrl();
    }

    public void setPermissions(String queueArn, String topicArn, String queueUrl){
        String policy = "{" +
                "\"Version\": \"2012-10-17\"," +
                "\"Statement\": [" +
                    "{" +
                        "\"Sid\":  \"topic-subscription-" + topicArn + "\","+
                        "\"Effect\": \"Allow\"," +
                        "\"Principal\": {" +
                            "\"AWS\": \"*\"" +
                        "}," +
                        "\"Action\": \"SQS:SendMessage\"," +
                        "\"Resource\": \"" + queueArn + "\"," +
                        "\"Condition\": {" +
                            "\"ArnLike\": {" +
                                "\"aws:SourceArn\": \"" + topicArn + "\"" +
                            "}" +
                        "}" +
                    "}" +
                "]" +
        "}";

        Map<QueueAttributeName, String> attributesMap = Map.of(QueueAttributeName.POLICY, policy);
        SetQueueAttributesRequest setQueueAttributesRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributesMap)
                .build();
        sqsClient.setQueueAttributes(setQueueAttributesRequest);
    }

    public String subscribeQueueToSNS(String topicArn, String queueArn){
        SubscribeRequest request = SubscribeRequest.builder()
                .protocol("sqs")
                .endpoint(queueArn)
                .topicArn(topicArn)
                .build();
        SubscribeResponse result = snsClient.subscribe(request);
        return result.subscriptionArn();
    }

    public void setFilter(String subscriptionArn) {
        String categoriesWithBrackets = "{\"category\": " + "[" + categories + "]}";
        SetSubscriptionAttributesRequest request = SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName("FilterPolicy")
                .attributeValue(categoriesWithBrackets)
                .build();
        snsClient.setSubscriptionAttributes(request);
    }


    public List<Message> getMessages(String queueUrl){
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .build();
        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        return response.messages();
    }

    public void bid(String queueUrl, Integer bidAmount, String email, String acceptanceQueueURL) {
        String body = "{\n" +
                "\"email\": \"" + email + "\",\n" +
                "\"bidAmount\": " + bidAmount + ",\n" +
                "\"acceptanceQueueURL\": \"" + acceptanceQueueURL + "\""
                + "}";
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build();
        sqsClient.sendMessage(request);
    }

    public void clearQueue(String queueUrl){
        PurgeQueueRequest request = PurgeQueueRequest.builder()
                .queueUrl(queueUrl)
                .build();
        sqsClient.purgeQueue(request);
    }
}
