package io.seda.inventory.services

import io.seda.inventory.auth.UserPrincipal
import io.seda.inventory.data.User
import io.seda.inventory.data.UserRepository
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.BadRequest
import reactor.core.publisher.Mono
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
class UserService {

    @Autowired lateinit var userRepository: UserRepository;
    @Autowired lateinit var jwtService: JWTService;
    @Autowired lateinit var passwordEncoder: PasswordEncoder;
    @Autowired lateinit var turnstileService: TurnstileService;

    suspend fun register(username: String, password: String, nickname: String): String {
        if (userRepository.findByUsername(username) != null) throw IllegalArgumentException("User already exists");
        var user = User(
            username = username,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            authority = listOf("ROLE_STUDENT")
        );
        user = userRepository.save(user);
        return jwtService.generateJWTFor(user.id!!, user.authority);
    }

    @PreAuthorize("isAuthenticated()")
    suspend fun changeName(nickname: String): String {
        val ctx = coroutineContext[ReactorContext.Key]?.context?.get<Mono<SecurityContext>>(SecurityContext::class.java)?.awaitSingle() ?: throw IllegalStateException("Not authenticated?");
        val user = ctx.authentication.principal as UserPrincipal;

        var entityUser = userRepository.findById(user.id) ?: throw IllegalStateException("You're not found");
        entityUser.nickname = nickname;
        entityUser = userRepository.save(entityUser);


        return jwtService.generateJWTFor(entityUser.id!!, entityUser.authority);
    }

    @PreAuthorize("isAuthenticated")
    suspend fun changePassword(previous: String, new: String) {
        val ctx = coroutineContext[ReactorContext.Key]?.context?.get<Mono<SecurityContext>>(SecurityContext::class.java)?.awaitSingle() ?: throw IllegalStateException("Not authenticated?");
        val user = ctx.authentication.principal as UserPrincipal;

        var entityUser = userRepository.findById(user.id) ?: throw IllegalStateException("You're not found");

        if (!passwordEncoder.matches(previous, entityUser.password)) throw IllegalArgumentException("Invalid Credentials");
        entityUser.password = passwordEncoder.encode(new);
        userRepository.save(entityUser);
    }

    suspend fun guestLogin(token: String): String {
        println(token)
        val isVerify = turnstileService.verify(token)
        println(isVerify)
        if(isVerify) {
            val uuid = UUID.randomUUID();
            println(uuid)
            return jwtService.generateJWTForGuest(uuid, listOf("ROLE_GUEST"));
        } else {
            throw Exception("turnstile verification failed")
        }
    }
}
