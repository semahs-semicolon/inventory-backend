package io.seda.inventory.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.impl.JWTParser
import io.seda.inventory.auth.JWTUserDetails
import jakarta.ws.rs.SeBootstrap.Instance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class JWTService {
    @Autowired
    lateinit var jwtAlgorithm: Algorithm;

    val decoder: JWTVerifier by lazy {
        JWT.require(jwtAlgorithm)
            .acceptLeeway(1000)
            .withClaimPresence("sub")
            .withClaimPresence("authorities").build();
    }

    fun generateJWTFor(userId: Long, authority: List<String>): String {
        return JWT.create()
            .withSubject(userId.toULong().toString())
            .withIssuedAt(Instant.now())
            .withClaim("authorities", authority)
            .withExpiresAt(Instant.now() + Duration.ofDays(7))
            .sign(jwtAlgorithm);
    }
    fun generateJWTForGuest(uuid: UUID, authority: List<String>): String {
        return JWT.create()
            .withSubject(uuid.toString())
            .withIssuedAt(Instant.now())
            .withClaim("authorities", authority)
            .withExpiresAt(Instant.now() + Duration.ofHours(1))
            .sign(jwtAlgorithm);
    }
    fun validateJWT(jwt: String): JWTUserDetails {
        val decoded = decoder.verify(jwt);
        return JWTUserDetails(
            decoded.getClaim("sub").asString().toULong().toLong(),
            decoded.getClaim("authorities").asList(String::class.java)
        )
    }
}
