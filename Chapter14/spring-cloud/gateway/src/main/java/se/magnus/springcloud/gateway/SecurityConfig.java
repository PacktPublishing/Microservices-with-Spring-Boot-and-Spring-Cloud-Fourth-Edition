
package se.magnus.springcloud.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable()
        .authorizeExchange(exchange -> exchange
          .pathMatchers("/headerrouting/**").permitAll()
          .pathMatchers("/actuator/**").permitAll()
          .pathMatchers("/eureka/**").permitAll()
          .pathMatchers("/oauth2/**").permitAll()
          .pathMatchers("/login/**").permitAll()
          .pathMatchers("/error/**").permitAll()
          .pathMatchers("/openapi/**").permitAll()
          .pathMatchers("/config/**").permitAll()
          .anyExchange().authenticated()))
      .oauth2ResourceServer(server -> server
        .jwt(Customizer.withDefaults()));
    return http.build();
  }

}