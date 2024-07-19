package io.seda.inventory.configuration

import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


@Configuration
class JWTConfiguration {
    @Value("\${jwt.private}")
    lateinit var privateKeyName: String;

    @Value("\${jwt.public}")
    lateinit var publicKeyName: String;

    @Value("\${jwt.random}")
    lateinit var randomKey: String;
    @Bean
    fun keyPair(): KeyPair{
        var client: SsmClient = SsmClient.create();

        if (randomKey.toBoolean()) {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            return keyPairGenerator.generateKeyPair();
        } else {
            val publicKey = client.getParameter(GetParameterRequest.builder().withDecryption(true).name(publicKeyName).build()).parameter().value();
            val privateKey = client.getParameter(GetParameterRequest.builder().withDecryption(true).name(privateKeyName).build()).parameter().value();

            val privateKeySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            val publicKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            val keyFactory = KeyFactory.getInstance("RSA");
            val privateRSAKey = keyFactory.generatePrivate(privateKeySpec);
            val publicRSAKey = keyFactory.generatePublic(publicKeySpec);

            return KeyPair(publicRSAKey, privateRSAKey);
        }
    }

    @Bean
    fun JWTAlgorithm(keyPair: KeyPair): Algorithm {
        return Algorithm.RSA512(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey);
    }
}