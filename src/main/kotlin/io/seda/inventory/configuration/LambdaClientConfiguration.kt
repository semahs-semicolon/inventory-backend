package io.seda.inventory.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI
import java.net.URISyntaxException
import java.time.Duration

@Configuration
class LambdaClientConfiguration {

    @Bean
    @Throws(URISyntaxException::class)
    fun sagemakerClient(awsCredentialsProvider: AwsCredentialsProvider?): LambdaAsyncClient {
        val httpClient = NettyNioAsyncHttpClient.builder()
            .writeTimeout(Duration.ZERO)
            .maxConcurrency(100)
            .connectionAcquisitionTimeout(Duration.ofMillis(50000))
            .maxPendingConnectionAcquires(10)
            .build()
        val clientBuilder = LambdaAsyncClient.builder()
            .httpClient(httpClient)
            .credentialsProvider(awsCredentialsProvider)
        return clientBuilder.build()
    }

}