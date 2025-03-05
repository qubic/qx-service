package org.qubic.qx.sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // not needed atm, but it would work (except test)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login").permitAll() // accessible for all
                        .pathMatchers("/service/v1/status/*").permitAll() // accessible for all
                        .pathMatchers("/actuator/health").permitAll() // for health monitoring
                        .anyExchange().authenticated() // other urls
                )
                .httpBasic(withDefaults())
                .formLogin(withDefaults()) // login form
                .build();
    }

}
