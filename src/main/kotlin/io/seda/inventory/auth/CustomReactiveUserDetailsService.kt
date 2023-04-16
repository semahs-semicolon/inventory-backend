package io.seda.inventory.auth

import io.seda.inventory.auth.FullUserDetails
import io.seda.inventory.data.UserRepository
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Component
class CustomReactiveUserDetailsService: ReactiveUserDetailsService {
    @Autowired
    lateinit var labelerRepository: UserRepository;
    override fun findByUsername(username: String?): Mono<UserDetails> = mono {
        val labeler = labelerRepository.findByUsername(username ?: throw BadCredentialsException("Invalid Credentials")) ?: throw BadCredentialsException("Invalid Credentials");
        return@mono FullUserDetails(labeler.id!!, labeler.nickname, labeler.username, labeler.password, labeler.authority);
    }
}