import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class SecureContextFilterTest : WebFilter {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .publishOn(Schedulers.boundedElastic())
            .map { response ->
                log.info("Request URI: {}", exchange.request.uri)

                ReactiveSecurityContextHolder.getContext()
                    .map { context -> context.authentication }
                    .subscribe { authentication ->
                        if (authentication != null) {
                            log.info("Authentication: {}", authentication)
                            log.info("Principal: {}", authentication.principal)
                            log.info("Authorities: {}", authentication.authorities)
                        } else {
                            log.info("Authentication is null")
                        }
                    }

                response
            }
    }
}
