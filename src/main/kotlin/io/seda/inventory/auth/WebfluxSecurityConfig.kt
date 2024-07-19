package io.seda.inventory.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.codec.json.AbstractJackson2Decoder
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import io.seda.inventory.services.JWTService


@Configuration
@EnableWebFluxSecurity
class WebfluxSecurityConfig {
    @Bean
    fun jacksonDecoder(): AbstractJackson2Decoder = Jackson2JsonDecoder()

    @Bean
    fun configureSecurity(http: ServerHttpSecurity, jwtAuthenticationFilter: AuthenticationWebFilter, jwtService: JWTService): SecurityWebFilterChain {
        return http
            .csrf {
                it.disable()
            }
            .logout {
                it.disable()
            }
            .httpBasic {
                it.disable()
            }
            .formLogin {
                it.disable()
            }
            .authorizeExchange {
                it.pathMatchers("/signin").permitAll()
                  .pathMatchers("/users/signup").permitAll()
                  .pathMatchers("/users/**").authenticated()
                  .pathMatchers(HttpMethod.GET, "/images/**").permitAll()
                  .anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(JWTReactiveAuthorizationFilter(jwtService), SecurityWebFiltersOrder.AUTHORIZATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .cors {
                it.configurationSource(corsWebFilter())
            }
            .build()
    }



    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationWebFilter(reactiveAuthenticationManager: ReactiveAuthenticationManager,
                                jwtConverter: ServerAuthenticationConverter,
                                serverAuthenticationSuccessHandler: ServerAuthenticationSuccessHandler,
                                jwtServerAuthenticationFailureHandler: ServerAuthenticationFailureHandler
    ): AuthenticationWebFilter {
        val authenticationWebFilter = AuthenticationWebFilter(reactiveAuthenticationManager)
        authenticationWebFilter.setRequiresAuthenticationMatcher { ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/signin").matches(it) }
        authenticationWebFilter.setServerAuthenticationConverter(jwtConverter)
        authenticationWebFilter.setAuthenticationSuccessHandler(serverAuthenticationSuccessHandler)
        authenticationWebFilter.setAuthenticationFailureHandler(jwtServerAuthenticationFailureHandler)
        authenticationWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        return authenticationWebFilter
    }

    @Bean
    fun reactiveAuthenticationManager(reactiveUserDetailsService: CustomReactiveUserDetailsService,
                                      passwordEncoder: PasswordEncoder): ReactiveAuthenticationManager {
        val manager = UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService)
        manager.setPasswordEncoder(passwordEncoder)
        return manager
    }

    @Bean
    fun corsWebFilter(): CorsConfigurationSource {
        val corsConfig = CorsConfiguration()
        corsConfig.allowedOrigins = listOf("https://inventory.seda.club/", "https://internal.inventory.seda.club/", "http://localhost:5173/", "http://100.77.45.97:5173/")
        corsConfig.maxAge = 8000L
        corsConfig.allowedMethods = mutableListOf("POST", "GET", "DELETE", "PATCH", "PUT")
        corsConfig.addAllowedHeader("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return source
    }
}