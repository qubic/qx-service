package org.qubic.qx.sync.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class RequestLoggingFilter implements WebFilter {

    private final boolean logUrl;
    private final boolean logHeaders;
    private final boolean logBody;

    public RequestLoggingFilter(boolean logUrl, boolean logHeaders, boolean logBody) {
        this.logUrl = logUrl;
        this.logHeaders = logHeaders;
        this.logBody = logBody;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (logUrl) {
            log.info("{} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        }
        if (logHeaders) {
            log.info("Request headers: {}", exchange.getRequest().getHeaders());
        }
        return logBody ? addBodyDecorator(exchange, chain) : chain.filter(exchange);
    }

    private static Mono<Void> addBodyDecorator(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchangeDecorator decorator = new ServerWebExchangeDecorator(exchange) {
                    @Override
                    public ServerHttpRequest getRequest() {
                        return new RequestLoggingDecorator(exchange.getRequest());
                    }
                };
        return chain.filter(decorator);
    }
}
