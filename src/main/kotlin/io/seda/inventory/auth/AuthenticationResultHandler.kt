package io.seda.inventory.auth

import io.seda.inventory.auth.FullUserDetails
import io.seda.inventory.auth.HttpExceptionFactory.badRequest
import io.seda.inventory.auth.HttpExceptionFactory.unauthorized
import io.seda.inventory.auth.JWTUserDetails
import io.seda.inventory.auth.UserPrincipal
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.core.ResolvableType
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.AbstractJackson2Decoder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import io.seda.inventory.services.JWTService

data class LoginRequest(val username: String, val password: String);

@Component
class LoginRequestConverter(private val jacksonDecoder: AbstractJackson2Decoder) : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> = mono {
        val loginRequest = getUsernameAndPassword(exchange!!) ?: throw badRequest()

        return@mono UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
    }

    private suspend fun getUsernameAndPassword(exchange: ServerWebExchange): LoginRequest? {
        val dataBuffer = exchange.request.body
        val type = ResolvableType.forClass(LoginRequest::class.java)
        return jacksonDecoder
            .decodeToMono(dataBuffer, type, MediaType.APPLICATION_JSON, mapOf())
            .onErrorResume { Mono.empty<LoginRequest>() }
            .cast(LoginRequest::class.java)
            .awaitFirstOrNull()
    }
}

@Component
class AuthenticationSuccessHandler(private val jwtService: JWTService) : ServerAuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(webFilterExchange: WebFilterExchange?, authentication: Authentication?): Mono<Void> {
        val principal = authentication?.principal ?: throw unauthorized() ;

        when (principal) {
            is FullUserDetails -> {
                val accessToken = jwtService.generateJWTFor(principal.id, principal.authorities.map { it.authority })
                return webFilterExchange?.exchange?.response?.writeWith(Mono.just(webFilterExchange.exchange.response.bufferFactory().wrap( accessToken.toByteArray())))!!
            }
            is JWTUserDetails -> {
                val accessToken = jwtService.generateJWTFor(principal.id, principal.authorities.map { it.authority })
                return webFilterExchange?.exchange?.response?.writeWith(Mono.just(webFilterExchange.exchange.response.bufferFactory().wrap( accessToken.toByteArray())))!!
            }
        }
        return Mono.empty()
    }
}

@Component
class JWTServerAuthenticationFailureHandler : ServerAuthenticationFailureHandler {
    override fun onAuthenticationFailure(webFilterExchange: WebFilterExchange?, exception: AuthenticationException?): Mono<Void> = mono {
        val exchange = webFilterExchange?.exchange ?: throw unauthorized()
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.setComplete().awaitFirstOrNull()
    }
}