package br.com.matheus.player.configuration.security;

import static org.springframework.security.config.Customizer.withDefaults;

import br.com.matheus.player.controller.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${api.secret}")
  private String apiSecret;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/**").permitAll()
        .anyRequest().authenticated())
        .httpBasic(withDefaults())
        .sessionManagement(s -> s
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new AuthenticationFilter(apiSecret), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}