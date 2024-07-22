package io.seda.inventory.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI
import java.net.URISyntaxException
import java.time.Duration

@Configuration
class S3ClientConfiguration {
    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    @Throws(URISyntaxException::class)
    fun s3AsyncClient(awsCredentialsProvider: AwsCredentialsProvider?): S3AsyncClient {
        val httpClient = NettyNioAsyncHttpClient.builder()
            .writeTimeout(Duration.ZERO)
            .maxConcurrency(100)
            .connectionAcquisitionTimeout(Duration.ofMillis(50000))
            .maxPendingConnectionAcquires(10)
            .build()
        val configuration = S3Configuration.builder()
            .checksumValidationEnabled(false)
            .chunkedEncodingEnabled(true)
            .build()
        val clientBuilder = S3AsyncClient.builder()
            .httpClient(httpClient)
            .credentialsProvider(awsCredentialsProvider)
            .serviceConfiguration(configuration)
        return clientBuilder.build()
    }

}