package com.sovreign.gateway.filter

import com.sovreign.gateway.service.proxy.AuthFilterServiceProxy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthPreFilter : GlobalFilter {

    private val dev = false //If dev is true the authentication filter is ignored

    val LOG = LoggerFactory.getLogger(AuthPreFilter::class.java)

    @Autowired
    lateinit var proxy: AuthFilterServiceProxy

    private val skipPaths = listOf(
            "/authenticationService/session/login",
            "/authenticationService/session/resumeSession",
            "/config.js",
            "/authenticationService/account/createNewAccount",
            "/api/dbcontroller",
            "/favicon.ico"
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        LOG.info("Entering filter")
        var res = proxy.test()
        LOG.info("From proxyTest: "+ res)
        if (dev) {
            return chain.filter(exchange)
        }
        val req = exchange.request
        LOG.info("Starting Transaction for req :{}", req.uri)
        val path: String = req.uri.rawPath

        if (!skipPaths.contains(path)) {
            val auth = req.headers.get("Auth")
            val username = req.headers.get("Username")
            if (!auth.isNullOrEmpty() && !username.isNullOrEmpty()) {
                val isAuthenticated = proxy.checkAuthentication(username[0], auth[0])
                if (isAuthenticated) {
                    return chain.filter(exchange)
                } else {
                    LOG.info("Not authenticated")
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            } else {
                LOG.info("No header set")
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        LOG.info("Committing Transaction for req :{}", req.uri)
        return chain.filter(exchange)
    }
}