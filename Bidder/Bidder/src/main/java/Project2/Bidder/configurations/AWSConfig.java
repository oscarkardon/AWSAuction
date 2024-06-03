package Project2.Bidder.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AWSConfig {
    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.accessKey}")
    private String awsAccessKey;

    @Value("${aws.secretKey}")
    private String awsSecretKey;

    @Value("${aws.sessionToken}")
    private String awsSessionToken;

    @Value("${auctionCompleteArn}")
    public String auctionCompleteArn;

    @Value("${forSaleArn}")
    public String forSaleArn;

    @Value("${bidderName}")
    public String bidderName;

    @Value("${bidderEmail}")
    public String bidderEmail;
    @Value("${accountId}")
    public String accountId;
    @Bean
    public String forSaleArn() {
        return forSaleArn;
    }

    @Bean
    public String auctionCompleteArn() {
        return auctionCompleteArn;
    }

    @Value("${categories}")
    public String categories;

    @Bean
    public String categories() {
        return categories;
    }


    @Bean
    public String bidderName() {
        return bidderName;
    }

    @Bean
    public String bidderEmail() {
        return bidderEmail;
    }

    @Bean
    public SqsClient sqsClient() {
        AwsSessionCredentials credentials = AwsSessionCredentials.create(awsAccessKey,awsSecretKey,awsSessionToken);
        return SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(() -> credentials)
                .build();
    }
    @Bean
    public SnsClient snsClient() {
        AwsSessionCredentials credentials = AwsSessionCredentials.create(awsAccessKey,awsSecretKey,awsSessionToken);
        return SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(() -> credentials)
                .build();
    }
}
