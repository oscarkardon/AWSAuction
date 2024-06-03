package Project2.Seller.configuration;

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
    @Bean
    public String auctionCompleteArn() {
        return auctionCompleteArn;
    }

    @Value("${forSaleArn}")
    public String forSaleArn;

    @Bean
    public String forSaleArn() {
        return forSaleArn;
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
