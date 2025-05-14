package se.magnus.springcloud.configserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    // Disable CSRF to allow POST to /encrypt and /decrypt endpoints
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
