package team.mistake.rlhfbackend.configuration

import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    lateinit var privateKey: String;

    @Value("\${jwt.public}")
    lateinit var publicKey: String;

    @Value("\${jwt.random}")
    lateinit var randomKey: String;
    @Bean
    fun keyPair(): KeyPair{
        if (randomKey.toBoolean()) {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            return keyPairGenerator.generateKeyPair();
        } else {
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