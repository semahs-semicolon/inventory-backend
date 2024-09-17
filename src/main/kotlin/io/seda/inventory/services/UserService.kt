package io.seda.inventory.services

import io.seda.inventory.auth.UserPrincipal
import io.seda.inventory.data.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

@Service
class UserService {

    @Autowired lateinit var userRepository: UserRepository;
    @Autowired lateinit var verifyCodeRepository: VerifyCodeRepository;
    @Autowired lateinit var identifierRepository: IdentifierRepository;
    @Autowired lateinit var jwtService: JWTService;
    @Autowired lateinit var passwordEncoder: PasswordEncoder;
    @Autowired lateinit var turnstileService: TurnstileService;

    suspend fun register(username: String, password: String, nickname: String, verifyCode: String): String {
        if (userRepository.findByUsername(username) != null) throw IllegalArgumentException("User already exists");
        val code = verifyCodeRepository.findByCode(verifyCode) ?: throw IllegalArgumentException("Invalid verify code");
        if(userRepository.findByIdentifier(code.identifier) != null) throw IllegalArgumentException("Identifier already used");
        var user = User(
            username = username,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            authority = code.authority,
            identifier = code.identifier
        );
        user = userRepository.save(user);
        verifyCodeRepository.delete(code);
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
        if(turnstileService.verify(token)) {
            val uuid = Random.Default.nextLong()
            return jwtService.generateJWTForGuest(uuid, listOf("ROLE_GUEST"))
        } else {
            throw Exception("turnstile verification failed")
        }

    }

    suspend fun createVerifyCode(identifier: String, authority: List<String>): VerifyCode {
        val code = Random.Default.nextBytes(4).joinToString("") { "%02x".format(it) }
        var verifyCode = VerifyCode(code = code, identifier = identifier, authority = authority)
        verifyCode = verifyCodeRepository.save(verifyCode);
        return verifyCode;
    }
    suspend fun invokeVerifyCode(code: String): VerifyCode {
        val verifyCode = verifyCodeRepository.findByCode(code) ?: throw IllegalArgumentException("Invalid verify code");
        verifyCodeRepository.delete(verifyCode);
        return verifyCode;
    }
    suspend fun createIdentifier(metadata: String): Identifier {
        val code = Random.Default.nextBytes(8).joinToString("") { "%02x".format(it) }
        var identifier = Identifier(identifierCode = code, metadata = metadata);
        identifier = identifierRepository.save(identifier);
        return identifier;
    }
    suspend fun invokeIdentifier(identifier: String): Identifier {
        val id = identifierRepository.findByIdentifierCode(identifier) ?: throw IllegalArgumentException("Invalid identifier");
        identifierRepository.delete(id);
        return id;
    }
    suspend fun getUserMetadata(identifier: String): String {
        val id = userRepository.findByIdentifier(identifier) ?: throw IllegalArgumentException("Invalid identifier");
        val metadata = id.identifier?.let { identifierRepository.findByIdentifierCode(it) } ?: throw IllegalArgumentException("Invalid identifier");
        return metadata.metadata.toString();
    }
    suspend fun findVerifyCode(code: String): VerifyCode {
        return verifyCodeRepository.findByCode(code) ?: throw IllegalArgumentException("Invalid verify code");
    }
    suspend fun findIdentifier(identifier: String): Identifier {
        return identifierRepository.findByIdentifierCode(identifier) ?: throw IllegalArgumentException("Invalid identifier");
    }
    suspend fun findAllVerifyCodes(): List<VerifyCode> {
        return verifyCodeRepository.findAll().toList();
    }
    suspend fun findAllIdentifiers(): List<Identifier> {
        return identifierRepository.findAll().toList();
    }

}
